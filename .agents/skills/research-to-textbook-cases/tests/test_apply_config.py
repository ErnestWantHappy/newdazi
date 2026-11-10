"""教研课例 apply 脚本的配置路径解析回归。"""
import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "scripts"))
from apply_import import normalize_config_path
from common import PipelineError


class ApplyConfigTest(unittest.TestCase):
    def test_directory_becomes_application_druid(self):
        self.assertEqual(
            normalize_config_path("/data/apps/xueyeceping/shared/config/"),
            "/data/apps/xueyeceping/shared/config/application-druid.yml",
        )

    def test_optional_file_prefix_is_removed(self):
        self.assertEqual(
            normalize_config_path("optional:file:/data/apps/xueyeceping/shared/config"),
            "/data/apps/xueyeceping/shared/config/application-druid.yml",
        )

    def test_existing_yaml_is_preserved(self):
        self.assertEqual(
            normalize_config_path("/data/apps/xueyeceping/shared/config/db.yml"),
            "/data/apps/xueyeceping/shared/config/db.yml",
        )

    def test_empty_value_is_rejected(self):
        with self.assertRaises(PipelineError):
            normalize_config_path("")


if __name__ == "__main__":
    unittest.main()
