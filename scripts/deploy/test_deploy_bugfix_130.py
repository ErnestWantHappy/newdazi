"""发布门禁的离线回归；所有远程入口均为 mock，不读取凭据。"""
import contextlib
import copy
import importlib.util
import io
import json
import tarfile
import tempfile
import unittest
from pathlib import Path
from unittest.mock import MagicMock, Mock, patch

SCRIPT = Path(__file__).with_name("deploy_bugfix_130.py")
spec = importlib.util.spec_from_file_location("bugfix_release", SCRIPT)
d = importlib.util.module_from_spec(spec)
spec.loader.exec_module(d)


def baseline():
    return {"jar": d.OLD_JAR, "frontendRoot": d.OLD_FRONTEND, "jarSha256": d.OLD_JAR_SHA, "indexSha256": d.OLD_INDEX_SHA, "ActiveState": "active", "SubState": "running", "NRestarts": "0", "MainPID": "3160006"}


class DeploymentTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.jar = self.root / "built.jar"
        self.jar.write_bytes(b"test jar bytes")
        self.dist = self.root / "dist"
        self.dist.mkdir()
        (self.dist / "index.html").write_text("<html>new version</html>", encoding="utf-8")
        (self.dist / "static").mkdir()
        (self.dist / "static/app.js").write_bytes(b"console.log('test')")
        self.output = self.root / "release"

    def package(self):
        return d.package_artifacts(self.jar, self.dist, self.output)

    def test_import_and_inspect_without_manifest(self):
        transport = Mock()
        transport.connect.return_value = (Mock(), Mock(), "unused-placeholder")
        with patch.object(d, "ARTIFACT_DIR", self.root / "absent"), patch.object(d, "remote130", transport), patch.object(d, "current_release", return_value=baseline()), contextlib.redirect_stdout(io.StringIO()):
            self.assertEqual(d.main(["--mode", "inspect"]), 0)
        transport.connect.assert_called_once()

    def test_check_and_deploy_missing_manifest_never_connect(self):
        for mode in ("check", "deploy"):
            with self.subTest(mode=mode), patch.object(d, "ARTIFACT_DIR", self.root / "absent"), patch.object(d, "remote130", Mock()) as transport:
                with self.assertRaisesRegex(d.DeployError, "缺少制品清单"):
                    d.main(["--mode", mode])
                transport.connect.assert_not_called()

    def test_package_root_and_manifest(self):
        manifest = self.package()
        self.assertEqual(manifest["version"], "1.30.96")
        self.assertEqual(manifest["frontend"]["fileCount"], 2)
        with tarfile.open(self.output / "frontend.tar.gz") as archive:
            self.assertEqual(archive.getnames(), ["index.html", "static/app.js"])
        self.assertEqual(d.validate_artifacts(self.output), manifest)
        with self.assertRaisesRegex(d.DeployError, "拒绝覆盖"):
            self.package()

    def test_package_mode_never_connects(self):
        with patch.object(d, "package_artifacts", return_value={}) as package, patch.object(d, "remote130", Mock()) as transport, contextlib.redirect_stdout(io.StringIO()):
            self.assertEqual(d.main(["--mode", "package"]), 0)
            package.assert_called_once()
            transport.connect.assert_not_called()

    def test_package_missing_build(self):
        with self.assertRaisesRegex(d.DeployError, "打包输入缺失"):
            d.package_artifacts(self.root / "missing.jar", self.dist, self.output)
        self.assertFalse(self.output.exists())

    def test_manifest_corruption_and_missing_files(self):
        manifest = self.package()
        changes = [("version", "1.30.94"), ("release", "other"), ("sql", ["other.sql"])]
        for key, value in changes:
            with self.subTest(key=key):
                bad = copy.deepcopy(manifest)
                bad[key] = value
                (self.output / "manifest.json").write_text(json.dumps(bad), encoding="utf-8")
                with self.assertRaises(d.DeployError):
                    d.validate_artifacts(self.output)
        (self.output / "manifest.json").write_text(json.dumps(manifest), encoding="utf-8")
        (self.output / "ruoyi-admin.jar").write_bytes(b"different")
        with self.assertRaisesRegex(d.DeployError, "大小或 SHA"):
            d.validate_artifacts(self.output)
        (self.output / "ruoyi-admin.jar").unlink()
        with self.assertRaisesRegex(d.DeployError, "缺少制品"):
            d.validate_artifacts(self.output)

    def test_archive_traversal_rejected_even_when_hash_matches(self):
        manifest = self.package()
        archive_path = self.output / "frontend.tar.gz"
        with tarfile.open(archive_path, "w:gz") as archive:
            member = tarfile.TarInfo("../escape.js")
            member.size = 1
            archive.addfile(member, io.BytesIO(b"x"))
        entry = {"bytes": archive_path.stat().st_size, "sha256": d.sha256_file(archive_path)}
        manifest["files"]["frontend.tar.gz"] = entry
        manifest["frontend"].update(archiveBytes=entry["bytes"], archiveSha256=entry["sha256"])
        (self.output / "manifest.json").write_text(json.dumps(manifest), encoding="utf-8")
        with self.assertRaisesRegex(d.DeployError, "不安全"):
            d.validate_artifacts(self.output)

    def test_baselines_are_separate_and_drift_blocked(self):
        d.validate_baseline(baseline())
        self.assertIn("typing_quote_v1", d.OLD_JAR)
        self.assertIn("python_interactive_v1", d.OLD_FRONTEND)
        for key in baseline():
            if key == "MainPID":
                continue
            with self.subTest(key=key):
                changed = baseline()
                changed[key] = "wrong"
                with self.assertRaises(d.DeployError):
                    d.validate_baseline(changed)

    def metadata(self):
        target = {"jar": d.NEW_JAR, "frontendRoot": d.NEW_FRONTEND, "jarSha256": "a" * 64, "indexSha256": "b" * 64}
        files = {name: {"bytes": 123, "sha256": "c" * 64} for name in ("service.before", "nginx.before", "env.before", "jar.before", "index.before.html", "database.sql.gz")}
        files["jar.before"]["sha256"] = d.OLD_JAR_SHA
        files["index.before.html"]["sha256"] = d.OLD_INDEX_SHA
        return {"release": d.RELEASE, "version": d.VERSION, "baseline": baseline(), "target": target, "files": files}

    def test_manual_rollback_requires_current_release_and_matching_backup(self):
        meta = self.metadata()
        d.validate_rollback(meta, meta["target"], meta["files"])
        with self.assertRaisesRegex(d.DeployError, "当前不是"):
            d.validate_rollback(meta, baseline(), meta["files"])
        for key in ("release", "version"):
            bad = copy.deepcopy(meta)
            bad[key] = "other"
            with self.assertRaises(d.DeployError):
                d.validate_rollback(bad, meta["target"], meta["files"])
        bad_files = copy.deepcopy(meta["files"])
        bad_files["service.before"]["sha256"] = "d" * 64
        with self.assertRaisesRegex(d.DeployError, "不匹配"):
            d.validate_rollback(meta, meta["target"], bad_files)
        with self.assertRaisesRegex(d.DeployError, "备份路径"):
            d.rollback(None, None, "/tmp/another-backup")

    def test_auto_rollback_accepts_partial_switch_but_rejects_other_version(self):
        meta = self.metadata()
        partial = baseline()
        partial.update(jar=d.NEW_JAR, jarSha256=meta["target"]["jarSha256"])
        d.validate_rollback(meta, partial, meta["files"], manual=False)
        partial["frontendRoot"] = "/another-release/frontend"
        with self.assertRaisesRegex(d.DeployError, "其他发布"):
            d.validate_rollback(meta, partial, meta["files"], manual=False)

    def test_preflight_versions_and_activity(self):
        values = {"old": "1", "new_conflict": "0", "new_count": "0", "answer5": "0", "checkin10": "0", "judging": "0", "ai": "0", "index_exists": "1"}
        for key in (None, "old", "answer5", "new_conflict"):
            v = dict(values)
            if key:
                v[key] = "999"
            with patch.object(d, "current_release", return_value=baseline()), patch.object(d, "run_checked"), patch.object(d, "sudo_bash"), patch.object(d, "db_query", return_value=list(v.items())) as query:
                if key:
                    with self.assertRaises(d.DeployError):
                        d.preflight(None, None, "config")
                else:
                    d.preflight(None, None, "config")
                    sql = query.call_args.args[2]
                    self.assertIn("version_no='1.30.95'", sql)
                    self.assertIn("version_no='1.30.96'", sql)
                    self.assertNotIn("version_no='1.30.93'", sql)

    def test_stage_uses_exclusive_mkdir(self):
        with patch.object(d, "sudo_bash", side_effect=d.DeployError("already exists")) as shell, patch.object(d, "upload_file") as upload:
            with self.assertRaises(d.DeployError):
                d.stage(None, None, {})
            first = shell.call_args.args[2]
            self.assertIn("mkdir " + d.NEW_RELEASE, first)
            self.assertNotIn("mkdir -p", first)
            upload.assert_not_called()

    def test_failure_after_switch_invokes_auto_rollback(self):
        manifest = {"files": {"ruoyi-admin.jar": {"sha256": "a" * 64}}, "frontend": {"indexSha256": "b" * 64}}
        transport = Mock()
        transport.connect.return_value = (Mock(), Mock(), "unused-placeholder")
        with patch.object(d, "remote130", transport), patch.object(d, "validate_artifacts", return_value=manifest), patch.object(d, "db_config_path", return_value="cfg"), patch.object(d, "preflight", return_value={"release": baseline()}), patch.object(d, "backup", return_value={"dir": "backup"}), patch.object(d, "stage"), patch.object(d, "sudo_bash"), patch.object(d, "switch", side_effect=d.DeployError("switch failed")), patch.object(d, "rollback", return_value={}) as rollback, patch.object(d, "draft_update"), patch.object(d, "write_state"), contextlib.redirect_stdout(io.StringIO()), contextlib.redirect_stderr(io.StringIO()):
            with self.assertRaisesRegex(d.DeployError, "已自动回滚"):
                d.main(["--mode", "deploy"])
            self.assertEqual(rollback.call_args.kwargs, {"manual": False})

    def test_remote_errors_hide_authentication_output(self):
        transport = Mock()
        transport.run.return_value = (1, "sensitive-auth-output", "sensitive-auth-output")
        with patch.object(d, "remote130", transport):
            with self.assertRaises(d.DeployError) as result:
                d.run_checked(None, "cmd", "检查")
            self.assertNotIn("sensitive-auth-output", str(result.exception))

    def test_package_rejects_output_inside_dist(self):
        (self.dist / "output").mkdir()
        with self.assertRaisesRegex(d.DeployError, "拒绝夹带"):
            self.package()

    def test_frontend_index_and_count_mismatch(self):
        manifest = self.package()
        for key, value in (("fileCount", 99), ("indexSha256", "0" * 64)):
            changed = copy.deepcopy(manifest)
            changed["frontend"][key] = value
            (self.output / "manifest.json").write_text(json.dumps(changed), encoding="utf-8")
            with self.assertRaisesRegex(d.DeployError, "文件数或首页"):
                d.validate_artifacts(self.output)

    def test_generated_remote_python_compiles_without_execution(self):
        meta = self.metadata()
        evidence = {"metadata": meta, "files": meta["files"]}
        current = dict(meta["target"], MainPID="123")
        def capture(client, password, source, label):
            compile(source, label, "exec")
            return json.dumps(evidence)
        with patch.object(d, "current_release", return_value=current), patch.object(d, "remote_python", side_effect=capture), patch.object(d, "sudo_bash"), patch.object(d, "wait_healthy", return_value={}):
            d.rollback(None, None, d.BACKUP_ROOT + "/20261111_120000_" + d.RELEASE)
        with patch.object(d, "sudo_bash"), patch.object(d, "remote_python", side_effect=capture):
            d.backup(None, None, "/config.yml", {"release": baseline()}, {"files": {"ruoyi-admin.jar": {"sha256": "a" * 64}}, "frontend": {"indexSha256": "b" * 64}})

    def test_upload_is_exclusive_and_checks_size(self):
        client = MagicMock()
        with patch.object(d, "run_checked", side_effect=[d.sha256_file(self.jar), str(self.jar.stat().st_size)]):
            d.upload_file(client, self.jar, "/new-release/file.jar")
        client.open_sftp.return_value.open.assert_called_once_with("/new-release/file.jar", "wx")

    def test_update_sql_and_register_are_version_scoped(self):
        sql = d.SQL_PATH.read_text(encoding="utf-8")
        self.assertIn("WHERE NOT EXISTS", sql)
        self.assertIn("status = 'PUBLISHED'", sql)
        self.assertIn("COALESCE(published_at, NOW())", sql)
        self.assertNotIn("ALTER TABLE", sql)
        with patch.object(d, "db_query", side_effect=[[], [["174", d.VERSION, d.TITLE, "PUBLISHED"]]]) as query:
            self.assertEqual(d.register_update(None, None, "cfg")["version"], d.VERSION)
            self.assertIn("version_no='1.30.96'", query.call_args.args[2])


if __name__ == "__main__":
    unittest.main(verbosity=2)
