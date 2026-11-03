using System;
using System.Collections.Generic;
using System.IO;
using System.Reflection;
using System.Threading;
using System.Web.Script.Serialization;

// 独立验收进程只运行本地监听；不安装、不写注册表、不连接正式平台。
internal static class SeatHelperBrowserProbe {
    static Type Client=typeof(SeatHelper);
    static BindingFlags Flags=BindingFlags.NonPublic|BindingFlags.Static;
    static void Set(string key,object value) {Client.GetField(key,Flags).SetValue(null,value);}
    static object Call(string key) {return Client.GetMethod(key,Flags).Invoke(null,null);}
    static void Main(string[] args) {
        try {
            Set("Site",new Dictionary<string,object>{{"platformUrl","http://127.0.0.1:18124"},{"allowedOrigins",new object[]{"http://10.52.1.130","http://xxkj.xsedu.net.cn"}}});
            Set("Config",new Dictionary<string,object>{{"apiUrl","http://127.0.0.1:18124"},{"deviceId","fixture-device"},{"secret","fixture-only"}});
            Call("StartLocalListener");
            File.WriteAllText(args[0],new JavaScriptSerializer().Serialize(new {defaultBrowser=Call("DefaultBrowser")}),System.Text.Encoding.UTF8);
            using(var timer=new Timer(delegate {Call("RefreshIdentity");},null,0,500)) {
                var until=DateTime.UtcNow.AddSeconds(60);
                while(DateTime.UtcNow<until && !File.Exists(args[1]))Thread.Sleep(100);
            }
        }catch(Exception e) {Console.Error.WriteLine(e.GetBaseException().Message);Environment.ExitCode=1;}
        finally {Call("StopLocalListener");}
    }
}
