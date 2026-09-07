"""正式发布统一入口；用制品清单接续发布，不自动混入工作区的其他改动。"""
import argparse
import hashlib
import json
import re
import sys
import time
import zipfile
from datetime import datetime
from pathlib import Path
import requests
from deploy_transport import Remote, REPO, secret

REMOTE_ROOT = 'D:/program/3009dazipingtai'


def digest(data):
    return hashlib.sha256(data).hexdigest().upper()


def make_manifest(directory, baseline_index):
    original = json.loads((directory / 'manifest.json').read_text(encoding='utf-8-sig'))
    jar = digest((directory / 'ruoyi-admin.jar').read_bytes())
    front = digest((directory / 'frontend.zip').read_bytes())
    if jar != original['candidateSha256'].upper() or front != original['frontendZipSha256'].upper():
        raise ValueError('制品与已有 manifest 不一致，停止发布')
    with zipfile.ZipFile(directory / 'frontend.zip') as z:
        files = {}
        for n in z.namelist():
            if n.endswith('/'):
                continue
            if n.startswith('/') or '\\' in n or ':' in n or '..' in n.split('/') or n in files:
                raise ValueError('前端压缩包路径非法或重复')
            files[n] = digest(z.read(n))
    if files['index.html'] != original['indexSha256'].upper():
        raise ValueError('首页与已有 manifest 不一致')
    if not re.fullmatch(r'[A-Fa-f0-9]{64}', baseline_index):
        raise ValueError('必须给出已核实的线上前端基线 SHA-256')
    return dict(schema=1, jarSha256=jar, frontendZipSha256=front,
                indexSha256=files['index.html'], baselineSha256=original['baselineSha256'].upper(),
                baselineIndexSha256=baseline_index.upper(), frontendFiles=files, sql='none')


def admin_token():
    pwd = secret(r'平台管理员验收账号.*?密码：`([^`]+)`')
    response = requests.post('http://10.52.1.123:3010/prod-api/login',
                             json={'username': 'admin', 'password': pwd}, timeout=15).json()
    if response.get('code') != 200 or not response.get('token'):
        raise RuntimeError('发布前管理员鉴权未通过，未切换服务')
    return response['token']


def safe_id(value):
    if not re.fullmatch(r'[A-Za-z0-9][A-Za-z0-9_-]{2,100}', value):
        raise ValueError('发布或任务名称只能包含英文字母、数字、下划线和连字符')
    return value


def stage(remote, release, directory, manifest):
    target = REMOTE_ROOT + '/releases/' + safe_id(release)
    # 若已上传，不覆盖：服务器校验完整目录后再决定是否允许切换。
    remote.ps(f"New-Item -ItemType Directory '{target}/backend' -Force|Out-Null")
    for local, dest, expected in [('ruoyi-admin.jar', 'backend/ruoyi-admin.jar', manifest['jarSha256']),
                                  ('frontend.zip', 'frontend.zip', manifest['frontendZipSha256'])]:
        exists = remote.ps(f"if(Test-Path -LiteralPath '{target}/{dest}'){{(Get-FileHash -LiteralPath '{target}/{dest}').Hash}}")
        if exists:
            if exists.strip().upper() != expected:
                raise RuntimeError('目标 release 已有不同制品，必须使用新 release 名称')
        else:
            remote.sftp.put(str(directory / local), target + '/' + dest + '.upload')
            remote.ps(f"if((Get-FileHash '{target}/{dest}.upload').Hash -ne '{expected}'){{throw 'hash_mismatch'}};Move-Item -LiteralPath '{target}/{dest}.upload' -Destination '{target}/{dest}'")
    remote.ps(f"if(-not(Test-Path '{target}/frontend/index.html')){{if(Test-Path '{target}/frontend'){{if(@(Get-ChildItem '{target}/frontend' -Force).Count -gt 0){{throw 'partial_frontend_use_new_release'}}}};Expand-Archive -LiteralPath '{target}/frontend.zip' -DestinationPath '{target}/frontend' -Force}}", timeout=45)
    manifest_path = target + '/release-manifest.json'
    data = json.dumps(manifest, ensure_ascii=False, indent=2).encode('utf-8')
    try:
        old = remote.read(manifest_path)
    except FileNotFoundError:
        old = None
    if old is not None and json.loads(old) != manifest:
        raise RuntimeError('目标清单已存在且不一致，不覆盖')
    if old is None:
        remote.write(manifest_path, data)
    return target


def status(remote, job_id):
    data = remote.read(REMOTE_ROOT + '/deploy-jobs/' + safe_id(job_id) + '/state.json')
    return json.loads(data.decode('utf-8-sig'))


def main():
    p = argparse.ArgumentParser(description='3009/3010 固定发布入口，默认不发布；SSH 断开后用 status 接续')
    p.add_argument('action', choices=['inspect', 'check', 'deploy', 'status', 'rollback'])
    p.add_argument('--release')
    p.add_argument('--artifacts', type=Path)
    p.add_argument('--baseline-index')
    p.add_argument('--job')
    p.add_argument('--backup')
    p.add_argument('--wait', type=int, default=0, help='等待秒数；0 立即返回任务 ID')
    args = p.parse_args()
    with Remote() as remote:
        if args.action == 'inspect':
            print(remote.ps(r"$p=Get-ItemProperty 'HKLM:\SYSTEM\CurrentControlSet\Services\NewDaziBackend3009\Parameters';$c=Get-Content 'D:\programsoftware\nginx\nginx-1.29.4\conf\nginx.conf' -Raw;@{time=(Get-Date).ToString('s');directory=$p.AppDirectory;jarSha256=(Get-FileHash (Join-Path $p.AppDirectory 'backend\ruoyi-admin.jar')).Hash;frontendRoots=@([regex]::Matches($c,'(?m)^\s*root\s+(D:/program/3009dazipingtai/releases/[^;]+)\s*;')|ForEach-Object{$_.Groups[1].Value});stdout=$p.AppStdout;stderr=$p.AppStderr;environmentCount=@($p.AppEnvironmentExtra).Count;services=@(Get-Service NewDaziBackend3009,UnifiedNginx|Select-Object Name,@{n='status';e={$_.Status.ToString()}})}|ConvertTo-Json -Depth 5"))
            return
        if args.action == 'status':
            print(json.dumps(status(remote, args.job), ensure_ascii=False, indent=2))
            return
        release = safe_id(args.release or '')
        if args.action in ('check', 'deploy'):
            if not args.artifacts or not args.baseline_index:
                p.error('check/deploy 需要 --artifacts 和 --baseline-index')
            manifest = make_manifest(args.artifacts, args.baseline_index)
            stage(remote, release, args.artifacts, manifest)
        job_id = safe_id(datetime.now().strftime('%Y%m%d_%H%M%S_') + args.action + '_' + release)
        job = REMOTE_ROOT + '/deploy-jobs/' + job_id
        remote.ps(f"New-Item -ItemType Directory '{job}'|Out-Null;& icacls.exe '{job}' /inheritance:r /grant:r '*S-1-5-18:(OI)(CI)F' '*S-1-5-32-544:(OI)(CI)F'|Out-Null;if($LASTEXITCODE -ne 0){{throw 'acl_failed'}}")
        engine = (REPO / 'scripts/release-engine.ps1').read_text(encoding='utf-8-sig')
        remote.write(job + '/engine.ps1', engine.encode('utf-8-sig'))
        request = dict(action=args.action, release=release, jobId=job_id, healthToken=admin_token())
        if args.action == 'rollback':
            if not args.backup:
                p.error('rollback 需要已完成发布的 --backup 路径')
            request['backup'] = args.backup
        remote.write(job + '/request.json', json.dumps(request).encode())
        remote.write(job + '/state.json', json.dumps(dict(status='queued', release=release)).encode())
        # SYSTEM 计划任务不依赖 SSH 会话存活；任务设 15 分钟总上限，内部命令各有短超时。
        task = 'NewDaziDeploy_' + job_id
        script = f"""$a=New-ScheduledTaskAction -Execute 'powershell.exe' -Argument '-NoProfile -NonInteractive -ExecutionPolicy Bypass -File "{job}/engine.ps1" -RequestFile "{job}/request.json"';$s=New-ScheduledTaskSettingsSet -ExecutionTimeLimit (New-TimeSpan -Minutes 15);Register-ScheduledTask -TaskName '{task}' -Action $a -Settings $s -User 'SYSTEM' -RunLevel Highest|Out-Null;Start-ScheduledTask -TaskName '{task}';Write-Output 'started'"""
        remote.ps(script, timeout=20)
        print(json.dumps({'job': job_id, 'action': args.action, 'status': 'started'}, ensure_ascii=False), flush=True)
        (REPO / 'output/deploy-last-job.json').write_text(json.dumps({'job': job_id, 'release': release}), encoding='utf-8')
        deadline = time.monotonic() + min(max(args.wait, 0), 900)
        previous = None
        while time.monotonic() < deadline:
            result = status(remote, job_id)
            if result != previous:
                print(json.dumps(result, ensure_ascii=False), flush=True)
                previous = result
            if result.get('status') not in ('queued', 'running'):
                if result['status'] not in ('succeeded', 'checked', 'rolled-back'):
                    raise RuntimeError('发布未完成，请按任务状态恢复')
                return
            time.sleep(3)


if __name__ == '__main__':
    try:
        main()
    except Exception as exc:
        # 不转储请求响应或异常堆栈，其中可能包含凭据。
        print('操作未完成：' + type(exc).__name__ + '。请先 inspect/status 核对，不重复切换。', file=sys.stderr)
        sys.exit(1)
