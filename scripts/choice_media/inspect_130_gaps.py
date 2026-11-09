"""130 只读核对：私有图片目录可否由服务账户创建、前端是否已带图片 UI、后端是否缺接口。"""
import json
import re
import shlex
import sys
from datetime import datetime

from migrate_local import ROOT

sys.path.insert(0, str(ROOT / "scripts/deploy"))
import remote130

PROFILE = "/data/upload"
MEDIA_DIR = "/data/upload-question-images"


def main():
    jump, client, password = remote130.connect()
    out = {}

    def read(name, command, sudo=False):
        code, text, error = (remote130.sudo(client, password, command) if sudo
                             else remote130.run(client, command))
        out[name] = {"exit": code, "stdout": text.strip(), "stderr": error.strip()[:600]}
        print(f"### {name} (exit {code})\n{text.strip()[:2000]}\n")
        return text

    try:
        # 1) /data 与 profile 的属主权限；服务账户是否可在 /data 下建目录
        read("perm", f"stat -c '%U:%G %a %n' /data {shlex.quote(PROFILE)}; "
                     f"sudo -u admin2 test -w /data && echo DATA_WRITABLE || echo DATA_NOT_WRITABLE; "
                     f"sudo -u admin2 test -w {shlex.quote(PROFILE)} && echo PROFILE_WRITABLE || echo PROFILE_NOT_WRITABLE",
             sudo=True)
        read("data_children", "ls -ld /data/* 2>/dev/null | head -12")
        read("profile_children", f"ls -ld {shlex.quote(PROFILE)}/* 2>/dev/null | head -12")

        # 2) 线上前端是否已含图片录入 UI（存在即说明前端已带功能，后端缺失会让教师直接报错）
        site = read("site", "grep -rhm1 '^\\s*root ' /etc/nginx/sites-available/xueyeceping-130 | awk '{print $2}' | tr -d ';'").strip()
        read("frontend_media_ui", f"grep -rlE '题干图片|添加图片|question-images' {shlex.quote(site)}/static/js 2>/dev/null | head -10; "
                                 f"echo '---count:'; grep -rlE 'question-images' {shlex.quote(site)}/static/js 2>/dev/null | wc -l")
        read("frontend_chunks", f"ls -1 {shlex.quote(site)}/static/js | grep -iE 'QuestionMedia|ImportPreview|question' | head -20")

        # 3) 线上后端 JAR 是否含图片接口实现
        jar = read("jar", "systemctl show xueyeceping-130 -p ExecStart | grep -o '/[^ ]*\\.jar' | head -1").strip()
        read("jar_media", f"unzip -l {shlex.quote(jar)} 2>/dev/null | grep -icE 'QuestionImage|QuestionMedia' || echo 0; "
                          f"unzip -l {shlex.quote(jar)} 2>/dev/null | grep -oE 'BOOT-INF/lib/ruoyi-business[^ ]*' | head -2")
        read("live_api_probe", "for p in /prod-api/business/question-images/1/content /prod-api/business/questions/1; "
                               "do printf '%s ' $p; curl -s -o /dev/null -w '%{http_code}\\n' http://127.0.0.1$p; done")

        out["checkedAt"] = datetime.now().isoformat()
        target = ROOT / "output/choice-media/production-gaps-20261108.json"
        target.write_text(json.dumps(out, ensure_ascii=False, indent=2), encoding="utf-8")
        print("已保存：" + str(target.relative_to(ROOT)))
    finally:
        client.close()
        jump.close()


if __name__ == "__main__":
    main()
