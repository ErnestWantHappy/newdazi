using System;
using System.Collections.Generic;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Reflection;
using System.Text;
using System.Threading;
using Microsoft.Win32;
using System.Windows.Forms;
using System.Drawing;

// 运行真实客户端地址选择逻辑，不启动浏览器、不注册设备、不改自启项。
internal static class SeatHelperRegression {
    static Type Client = typeof(SeatHelper);
    static BindingFlags Flags = BindingFlags.NonPublic | BindingFlags.Static;
    static int Passed;
    static object Invoke(string name, params object[] args) { return Client.GetMethod(name,Flags).Invoke(null,args); }
    static void Set(string name, object value) { Client.GetField(name,Flags).SetValue(null,value); }
    static void Check(bool value,string name) { if(!value) throw new Exception(name); Passed++; Console.WriteLine("PASS " + name); }
    static string Fallback = "http://10.52.1.130";
    static string Resolve() { return (string)Invoke("ResolvePlatformUrl"); }
    [STAThread] static void Main(string[] args) {
        try {
            var root=Path.GetFullPath(args[0]); Directory.CreateDirectory(root);
            string preferred=Path.Combine(root,"preferred"), fallbackHome=Path.Combine(root,"fallback");
            Action<string> writable=delegate(string directory) { Directory.CreateDirectory(directory); };
            object[] homeArgs={preferred,fallbackHome,writable,null};
            Check((string)Client.GetMethod("SelectHome",Flags).Invoke(null,homeArgs)==preferred && (string)homeArgs[3]=="", "优先使用可写D盘候选目录");
            Action<string> deniedPreferred=delegate(string directory) { if(directory==preferred)throw new UnauthorizedAccessException(); Directory.CreateDirectory(directory); };
            homeArgs=new object[]{preferred,fallbackHome,deniedPreferred,null};
            Check((string)Client.GetMethod("SelectHome",Flags).Invoke(null,homeArgs)==fallbackHome && ((string)homeArgs[3]).Contains("已改用"),"D盘不可写时明确告知回退目录");
            Action<string> deniedAll=delegate(string directory) {throw new UnauthorizedAccessException();};
            bool noHome=false;try{Client.GetMethod("SelectHome",Flags).Invoke(null,new object[]{preferred,fallbackHome,deniedAll,null});}catch(TargetInvocationException){noHome=true;}
            Check(noHome,"两个位置都不可写时停止安装，不报成功");
            bool attempted=false;
            Action installDenied=delegate {throw new IOException();};
            Action success=delegate {attempted=true;};
            var install=(SeatHelper.InstallResult)Invoke("RunInstallSteps",installDenied,success);
            Check(attempted && install.StartupReady && !install.ShortcutReady,"快捷方式失败仍执行自启并单独报告");
            install=(SeatHelper.InstallResult)Invoke("RunInstallSteps",success,installDenied);
            Check(install.ShortcutReady && !install.StartupReady && install.StartupError!="","自启失败不显示成功");
            string sourceExe=Path.Combine(root,"download.exe"), installedExe=Path.Combine(preferred,"SeatHelper.exe");
            File.WriteAllText(sourceExe,"new-program-with-public-site-trailer");
            File.WriteAllText(installedExe,"old-program");
            string retainedConfig=Path.Combine(preferred,"seat-helper.dat");File.WriteAllText(retainedConfig,"existing-device-data");
            Invoke("CopyProgram",sourceExe,installedExe);
            Check(File.ReadAllText(installedExe)==File.ReadAllText(sourceExe) && File.ReadAllText(retainedConfig)=="existing-device-data","升级复制完整文件且保留设备关联数据");
            Invoke("CopyProgram",installedExe,installedExe);
            Check(File.ReadAllText(installedExe)==File.ReadAllText(sourceExe),"从安装目录启动不覆盖自身");
            bool failedCopy=false;try{Invoke("CopyProgram",Path.Combine(root,"missing.exe"),installedExe);}catch(TargetInvocationException){failedCopy=true;}
            Check(failedCopy && File.ReadAllText(installedExe)==File.ReadAllText(sourceExe) && Directory.GetFiles(preferred,"*.tmp").Length==0,"复制失败保留旧程序且清理临时文件");
            string shortcutPath=Path.Combine(root,"platform.lnk");
            Invoke("CreateShortcut",installedExe,shortcutPath);
            Check(File.Exists(shortcutPath),"真实COM创建测试目录快捷方式");
            string testKey=@"Software\SchoolSeatHelperRegression\"+Guid.NewGuid().ToString("N");
            try {
                using(var key=Registry.CurrentUser.CreateSubKey(testKey)) {
                    Invoke("WriteStartup",key,installedExe,true);
                    Check((string)key.GetValue("SchoolSeatHelper")=="\""+installedExe+"\" --background","自启命令保留路径引号和后台参数");
                    Invoke("WriteStartup",key,installedExe,false);
                    Check(key.GetValue("SchoolSeatHelper")==null,"取消自启只删除目标项");
                }
            } finally {Registry.CurrentUser.DeleteSubKeyTree(testKey,false);}
            Set("ConfigPath",retainedConfig);Set("Installation",new SeatHelper.InstallResult {StartupReady=true,ShortcutReady=true});Set("Status","已关联测试设备");
            using(var form=(Form)Invoke("CreateStatusWindow")) {
                form.StartPosition=FormStartPosition.Manual;form.Location=new Point(-10000,-10000);form.ShowInTaskbar=false;form.Show();form.PerformLayout();
                bool hasLocation=false,hasBaseline=false;
                foreach(Control control in form.Controls) {hasLocation|=control.Text.Contains(preferred);hasBaseline|=control.Text.Contains("还原基线");}
                Check(hasLocation && hasBaseline,"结果窗口显示实际安装位置与还原基线提示");
                using(var bitmap=new Bitmap(form.Width,form.Height)) {form.DrawToBitmap(bitmap,new Rectangle(0,0,form.Width,form.Height));bitmap.Save(Path.Combine(root,"installation-preview.png"));}
            }
            var package=Path.Combine(root,"public-site-package.exe");
            var publicInfo=Encoding.UTF8.GetBytes("{\"platformUrl\":\"https://school.example.cn\"}");
            using(var stream=File.Create(package)) {
                stream.Write(new byte[]{77,90},0,2);stream.Write(publicInfo,0,publicInfo.Length);
                var length=BitConverter.GetBytes(publicInfo.Length);stream.Write(length,0,length.Length);
                var marker=Encoding.ASCII.GetBytes("SEATHELPER-SITE01");stream.Write(marker,0,marker.Length);
            }
            var decoded=(Dictionary<string,object>)Invoke("ReadSiteFile",package);
            Check((string)decoded["apiUrl"]=="https://school.example.cn/prod-api" && decoded.Count==2,"实际文件尾部解析公开学校地址，不依赖默认站点");
            File.WriteAllBytes(package,new byte[]{77,90});
            decoded=(Dictionary<string,object>)Invoke("ReadSiteFile",package);
            Check((string)decoded["platformUrl"]==Fallback,"无站点尾部的原始程序使用正式公开入口");
            using(var stream=File.Create(package)) {
                var length=BitConverter.GetBytes(5000);stream.Write(length,0,4);
                var marker=Encoding.ASCII.GetBytes("SEATHELPER-SITE01");stream.Write(marker,0,marker.Length);stream.WriteByte(0);
            }
            var brokenBytes=File.ReadAllBytes(package);File.WriteAllBytes(package,new byte[1]);
            using(var stream=new FileStream(package,FileMode.Append))stream.Write(brokenBytes,0,brokenBytes.Length-1);
            bool malformed=false;try{Invoke("ReadSiteFile",package);}catch(TargetInvocationException){malformed=true;}
            Check(malformed,"拒绝损坏的公开站点尾部长度");
            Invoke("ValidateApi","http://10.52.1.130/prod-api");
            Invoke("ValidatePlatform",Fallback);
            Check(true,"现有内网HTTP站点和接口均可配置");
            Invoke("ValidatePlatform","https://school.example.cn");
            Check(true,"HTTPS配置继续兼容");
            foreach(var invalid in new[]{"file:///C:/Windows/notepad.exe","javascript:alert(1)","http://user:password@10.52.1.130","http://10.52.1.130?token=1","http://10.52.1.130#token","http://10.52.1.130:0"}) {
                bool rejected=false; try { Invoke("ValidateApi",invalid); } catch(TargetInvocationException) { rejected=true; }
                Check(rejected,"拒绝不合规地址"+Passed);
            }
            Set("ConfigPath",Path.Combine(root,"seat-helper.dat"));
            File.WriteAllText(Path.Combine(root,"platform-url.txt"),Fallback,Encoding.UTF8);
            var portProbe=new TcpListener(IPAddress.Loopback,0); portProbe.Start();
            int port=((IPEndPoint)portProbe.LocalEndpoint).Port; portProbe.Stop();
            string api="http://localhost:"+port;
            var config=new Dictionary<string,object>{{"apiUrl",api},{"deviceId","test-device"},{"secret","test-secret"}};
            Set("Config",config);
            using(var listener=new HttpListener()) {
                listener.Prefixes.Add(api+"/"); listener.Start();
                foreach(string failure in new[]{"学校已关闭","设备已撤销"}) {
                    var responder=new Thread(delegate() {
                        var request=listener.GetContext(); var bytes=Encoding.UTF8.GetBytes("{\"code\":500,\"msg\":\""+failure+"\"}");
                        request.Response.OutputStream.Write(bytes,0,bytes.Length); request.Response.Close();
                    }); responder.IsBackground=true; responder.Start();
                    Check(Resolve()==Fallback,failure+"仍返回普通网页"); responder.Join();
                }
                var denied=new Thread(delegate() { var request=listener.GetContext(); request.Response.StatusCode=503; request.Response.Close(); });
                denied.IsBackground=true; denied.Start(); Check(Resolve()==Fallback,"HTTP 503仍返回普通网页"); denied.Join();
                listener.Stop();
            }
            Check(Resolve()==Fallback,"接口不可达仍返回普通网页");
            File.WriteAllText(Path.Combine(root,"seat-helper.dat"),"损坏的配置",Encoding.UTF8);
            Invoke("LoadConfig");
            Check(Client.GetField("Config",Flags).GetValue(null)==null,"损坏配置回到可重新配置状态");
            Check(Resolve()==Fallback,"损坏配置仍能使用独立网页地址");
            File.WriteAllText(Path.Combine(root,"platform-url.txt"),"file:///C:/Windows/notepad.exe",Encoding.UTF8);
            Check((string)Invoke("ReadFallback")=="","不接受文件地址降级");
            Console.WriteLine("客户端回归通过："+Passed);
        } catch(Exception e) { Console.Error.WriteLine(e.GetBaseException().Message); Environment.ExitCode=1; }
    }
}

