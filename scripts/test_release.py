"""在临时目录模拟发布故障；不连接服务器、不启动服务、不修改真实注册表。"""
import hashlib
import json
import os
import subprocess
import tempfile
import unittest
from pathlib import Path

ENGINE=Path(__file__).with_name('release-engine.ps1')


class ReleaseRecoveryTests(unittest.TestCase):
    def simulate(self, fault):
        with tempfile.TemporaryDirectory(prefix='newdazi-release-test-') as td:
            base=Path(td);root=base/'production';nginx=base/'nginx';job=base/'job'
            old=root/'releases/old_v1';new=root/'releases/new_v1'
            for d in [job,nginx/'conf',old/'backend',old/'frontend',old/'config',new/'backend',new/'frontend',new/'config']:
                d.mkdir(parents=True)
            for d,data in [(old,b'old'),(new,b'new')]:
                (d/'backend/ruoyi-admin.jar').write_bytes(data)
                (d/'frontend/index.html').write_bytes(data+b'html')
                (d/'frontend.zip').write_bytes(data+b'zip')
                (d/'config/application-druid.yml').write_text('url: jdbc:mysql://127.0.0.1:3306/ry-vue?test=true\nusername: root\npassword: dummy\n')
            sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest().upper()
            before='server {\n listen 3010;\n root '+old.as_posix()+'/frontend;\n}\nserver { listen 80; }\n'
            (nginx/'conf/nginx.conf').write_text(before)
            manifest={'jarSha256':sha(new/'backend/ruoyi-admin.jar'),'frontendZipSha256':sha(new/'frontend.zip'),
                      'indexSha256':sha(new/'frontend/index.html'),'baselineSha256':sha(old/'backend/ruoyi-admin.jar'),
                      'baselineIndexSha256':sha(old/'frontend/index.html'),'frontendFiles':{'index.html':sha(new/'frontend/index.html')}}
            (new/'release-manifest.json').write_text(json.dumps(manifest))
            if fault=='hash':(new/'backend/ruoyi-admin.jar').write_bytes(b'tampered')
            request={'jobId':'test_job','release':'new_v1','action':'deploy','healthToken':'dummy'}
            (job/'request.json').write_text(json.dumps(request))
            code=ENGINE.read_text(encoding='utf-8-sig')
            code=code.replace("$reason=$_.Exception.GetType().Name+'@'+$_.InvocationInfo.ScriptLineNumber", "$reason=$_.Exception.Message")
            code=code.replace("$root = 'D:\\program\\3009dazipingtai'", "$root = '"+str(root)+"'")
            code=code.replace("$nginxHome = 'D:\\programsoftware\\nginx\\nginx-1.29.4'", "$nginxHome = '"+str(nginx)+"'")
            code=code.replace("$reg = 'HKLM:\\SYSTEM\\CurrentControlSet\\Services\\NewDaziBackend3009\\Parameters'", "$reg = 'TEST_ONLY'")
            code=code.replace("Global\\NewDaziProductionDeploy",'Local\\NewDaziDeployTest_'+base.name)
            mocks=r'''
$script:mock=@{AppDirectory=(Join-Path $root 'releases\old_v1');AppStdout='old.stdout';AppStderr='old.stderr';AppEnvironmentExtra=@(1..20|ForEach-Object{"DUMMY_$_=value"})}
$script:events=New-Object Collections.Generic.List[string]
function Get-ItemProperty($path){if($path -ne 'TEST_ONLY'){throw 'unsafe_test_registry'};[pscustomobject]$script:mock}
function Set-ItemProperty($path,$name,$value){if($path -ne 'TEST_ONLY'){throw 'unsafe_test_registry'};$script:mock[$name]=$value}
function Get-Service($name){[pscustomobject]@{Status='Running'}}
function icacls.exe {$global:LASTEXITCODE=0}
function Get-Live {
 $front=[regex]::Match([IO.File]::ReadAllText($nginxConf),'root ([^;]+);').Groups[1].Value
 @{directory=$script:mock.AppDirectory;frontend=$front;jarHash=(Hash (Join-Path $script:mock.AppDirectory 'backend\ruoyi-admin.jar'));indexHash=(Hash (Join-Path $front 'index.html'));nginxHash=(Hash $nginxConf);environmentCount=20}
}
function Test-Nginx($file){if(-not(Test-Path $file)){throw 'missing_conf'}}
function Reload-Nginx {$script:events.Add('reload')}
function Stop-Backend {$script:events.Add('stop')}
function Start-Backend {$script:events.Add('start')}
function Health($index,$seconds){if($script:phase -eq 'health' -and $fault -eq 'health'){throw 'injected_health_failure'}}
function Run-Exe($exe,$arguments,$seconds,$label){
 if($label -ne 'database-backup'){throw 'unexpected_test_process'}
 if($fault -eq 'backup'){throw 'injected_backup_failure'}
 [IO.File]::WriteAllText((Join-Path $script:backup 'ry-vue.sql'),(('x'*2048)+"`n-- Dump completed on test`n"))
}
'''
            code=code.replace('\ntry {\n    $locked=', "\n$fault='"+fault+"'\n"+mocks+'\ntry {\n    $locked=')
            code=code.replace("    $request.healthToken=''", "    @{directory=$script:mock.AppDirectory;stdout=$script:mock.AppStdout;events=@($script:events)}|ConvertTo-Json|Set-Content (Join-Path $job 'mock-final.json')\n    $request.healthToken=''")
            script=base/'simulation.ps1';script.write_text(code,encoding='utf-8-sig')
            env={k:v for k,v in os.environ.items() if k.upper()!='PSMODULEPATH'}
            run=subprocess.run(['powershell.exe','-NoProfile','-NonInteractive','-ExecutionPolicy','Bypass','-File',str(script),'-RequestFile',str(job/'request.json')],capture_output=True,timeout=30,env=env)
            self.assertTrue((job/'state.json').exists(),run.stderr.decode(errors='replace'))
            state=json.loads((job/'state.json').read_text(encoding='utf-8-sig'))
            final=json.loads((job/'mock-final.json').read_text(encoding='utf-8-sig'))
            req=json.loads((job/'request.json').read_text(encoding='utf-8-sig'))
            self.assertEqual(req['healthToken'],'')
            if fault=='none':
                self.assertEqual(state['status'],'succeeded',state)
                self.assertEqual(Path(final['directory']),new)
                self.assertIn(new.as_posix(),(nginx/'conf/nginx.conf').read_text())
            else:
                self.assertEqual(Path(final['directory']),old)
                self.assertEqual(final['stdout'],'old.stdout')
                self.assertEqual((nginx/'conf/nginx.conf').read_text(),before)
                self.assertEqual(state['status'],'rolled-back' if fault=='health' else 'failed',state)
                self.assertEqual(final['events'].count('stop'),2 if fault=='health' else 0)

    def test_success(self):self.simulate('none')
    def test_hash_mismatch_never_stops_service(self):self.simulate('hash')
    def test_backup_failure_never_stops_service(self):self.simulate('backup')
    def test_health_failure_restores_backend_frontend_and_logs(self):self.simulate('health')


if __name__=='__main__':unittest.main()
