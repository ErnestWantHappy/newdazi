using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Management;
using System.Net;
using System.Net.NetworkInformation;
using System.Security.Cryptography;
using System.Text;
using System.Threading;
using System.Web.Script.Serialization;
using System.Windows.Forms;
using Microsoft.Win32;

// 无第二套账号；凭据留在本机加密文件，一次性浏览器链接仅用于关联现有登录会话。
internal static class SeatHelper {
    static readonly JavaScriptSerializer Json = new JavaScriptSerializer();
    static string ConfigPath = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "seat-helper.dat");
    static Dictionary<string, object> Config;
    static NotifyIcon Tray;
    static Control Dispatcher;
    static int Busy;
    static string Status = "启动中";
    static Dictionary<string,object> Site;
    static Dictionary<string,object> Pending;
    static DateTime LastPulse=DateTime.MinValue;
    static DateTime PairUntil;
    static string HomeNotice = "";
    static InstallResult Installation;
    static Form StatusWindow;
    static bool Exiting;

    internal sealed class InstallResult {
        public bool ShortcutReady;
        public bool StartupReady;
        public string ShortcutError = "";
        public string StartupError = "";
    }
    [STAThread] static void Main(string[] args) {
        Application.EnableVisualStyles();
        ServicePointManager.SecurityProtocol = (SecurityProtocolType)3072;
        try {
            if (args.Length >= 2 && args[0] == "--self-test") {
                File.WriteAllText(args[1], Json.Serialize(new { ok = true, fingerprintLength = Fingerprint().Length, localIp = LocalIp(), runtime = Environment.Version.ToString() }), Encoding.UTF8); return;
            }
            if (args.Length >= 3 && args[0] == "--register-file") {
                var setup = Json.Deserialize<Dictionary<string, object>>(File.ReadAllText(args[1], Encoding.UTF8));
                ConfigPath = args[2]; Config = Register(Text(setup, "apiUrl"), Text(setup, "code")); Save(ConfigPath);
                File.WriteAllText(args[2] + ".result.json", Json.Serialize(new { ok = true, deviceId = Text(Config, "deviceId"), localIp = LocalIp() }), Encoding.UTF8); return;
            }
            if (args.Length >= 3 && args[0] == "--probe") {
                ConfigPath = args[1]; Config = Read();
                var data = Call("device/open", Auth());
                File.WriteAllText(args[2], Json.Serialize(data), Encoding.UTF8); return;
            }
            if (args.Length >= 3 && args[0] == "--auto-probe") {
                // 验收使用同一自动配对逻辑，不写桌面、自启或打开本机其他浏览器。
                ConfigPath=args[1];Site=ReadSite();Config=null;BeginAutomaticLink(false);
                File.WriteAllText(args[2],Json.Serialize(new {url=PairUrl(),localIp=LocalIp(),expiresIn=300}),Encoding.UTF8);
                while(DateTime.UtcNow<PairUntil && Config==null) {PollPair();Thread.Sleep(1000);}
                if(Config==null)throw new Exception("自动关联未完成");Pulse();return;
            }
            Site=ReadSite();PrepareHome(false);LoadConfig();
            bool created;
            using (var mutex = new Mutex(true, "Local\\SchoolSeatHelper", out created)) {
                if (!created) {
                    if (args.Contains("--configure")) MessageBox.Show("请先退出托盘中的助手，再运行重新配置。", "机位助手");
                    else if (!args.Contains("--background")) {
                        MessageBox.Show("机位助手已在后台运行。可从右下角托盘打开平台或查看状态。\n如果正在更新安装，请先在托盘中退出旧助手，再双击新程序。", "机位助手已运行");
                        if (Config != null) TryOpen();
                    }
                    return;
                }
                if (args.Contains("--configure")) {
                    if (!Configure()) return;
                }
                PrepareHome(true);
                var installedExe = Path.Combine(Path.GetDirectoryName(ConfigPath), "SeatHelper.exe");
                if (!string.Equals(installedExe, Application.ExecutablePath, StringComparison.OrdinalIgnoreCase)) {
                    // 释放单实例锁后交接给安装目录中的程序，避免一直运行桌面下载副本。
                    mutex.ReleaseMutex();
                    mutex.Dispose();
                    Process.Start(new ProcessStartInfo(installedExe, args.Contains("--background") ? "--background" : "") { UseShellExecute = true, WorkingDirectory = Path.GetDirectoryName(installedExe), WindowStyle = ProcessWindowStyle.Hidden });
                    return;
                }
                Dispatcher = new Control(); var dispatcherHandle = Dispatcher.Handle;
                Tray = new NotifyIcon { Icon = System.Drawing.SystemIcons.Application, Text = "学业测评机位助手", Visible = true };
                var menu = new ContextMenuStrip();
                menu.Items.Add("打开学业测评平台", null, delegate { ThreadPool.QueueUserWorkItem(delegate { TryOpen(); }); });
                menu.Items.Add("安装与运行状态", null, delegate { ShowStatusWindow(); });
                menu.Items.Add("重新自动关联",null,delegate {ThreadPool.QueueUserWorkItem(delegate {BeginAutomaticLink(true);});});
                menu.Items.Add("退出助手", null, delegate { Exiting = true; Tray.Visible = false; Application.Exit(); });
                Tray.ContextMenuStrip = menu;
                Tray.DoubleClick += delegate { ThreadPool.QueueUserWorkItem(delegate { TryOpen(); }); };
                Status = Config == null ? "尚未关联，请在平台登录学生账号完成关联" : "已读取本机关联信息，正在连接平台";
                if (!args.Contains("--background")) {
                    ShowStatusWindow();
                    ThreadPool.QueueUserWorkItem(delegate { TryOpen(); });
                }
                using (var timer = new System.Threading.Timer(delegate {
                    if(Pending!=null)PollPair();
                    else if(Config!=null && DateTime.UtcNow-LastPulse>=TimeSpan.FromSeconds(30)) {LastPulse=DateTime.UtcNow;Pulse();}
                },null,0,3000))Application.Run();
                if (StatusWindow != null) StatusWindow.Dispose();
                Tray.Dispose(); Dispatcher.Dispose();
            }
        } catch (Exception e) {
            // 自动启动失败只影响助手，不关闭浏览器，也不记录凭据或请求正文。
            if (args.Contains("--background")) return;
            if (args.Length > 0 && (args[0] == "--probe" || args[0] == "--auto-probe" || args[0] == "--register-file" || args[0] == "--self-test")) { Environment.ExitCode = 1; return; }
            MessageBox.Show("助手暂不可用，请正常登录网页使用平台。\n" + e.Message, "机位助手");
        }
    }
    static Dictionary<string,object> Register(string api,string code) {
        ValidateApi(api);
        var registration = new Dictionary<string,object> { {"apiUrl",api.TrimEnd('/')} };
        var data = Call("device/register", new Dictionary<string,object> { {"code",code.Trim()}, {"fingerprint",Fingerprint()} }, registration);
        data["apiUrl"] = api.TrimEnd('/'); data["fingerprint"] = Fingerprint(); return data;
    }
    static void LoadConfig() {
        try { Config = File.Exists(ConfigPath) ? Read() : null; }
        catch { Config = null; Status = "配置损坏或机器身份变化，请管理员重新配置"; }
    }
    static Dictionary<string,object> ReadSite() {return ReadSiteFile(Application.ExecutablePath);}
    static Dictionary<string,object> ReadSiteFile(string executable) {
        var site=new Dictionary<string,object>{{"platformUrl","http://10.52.1.130"}};
        using(var stream=File.OpenRead(executable)) {
            if(stream.Length>21) {
                stream.Seek(-21,SeekOrigin.End);var tail=new byte[21];stream.Read(tail,0,21);
                if(Encoding.ASCII.GetString(tail,4,17)=="SEATHELPER-SITE01") {
                    int length=BitConverter.ToInt32(tail,0);
                    if(length<=0||length>4096||length>stream.Length-21)throw new Exception("平台下载信息无效，请重新下载助手");
                    stream.Seek(-21-length,SeekOrigin.End);var bytes=new byte[length];stream.Read(bytes,0,length);
                    site=Json.Deserialize<Dictionary<string,object>>(Encoding.UTF8.GetString(bytes));
                }
            }
        }
        ValidatePlatform(Text(site,"platformUrl"));site["apiUrl"]=Text(site,"platformUrl").TrimEnd('/')+"/prod-api";return site;
    }
    // 写入探测与安装分开，D盘不可写时必须在结果页明确告知实际位置。
    static string SelectHome(string preferred, string fallback, Action<string> ensureWritable, out string notice) {
        try { ensureWritable(preferred); notice = ""; return preferred; }
        catch { ensureWritable(fallback); notice = "D盘不存在或无法写入，已改用当前用户目录。请老师核对还原保护设置。"; return fallback; }
    }
    static void EnsureWritable(string home) {
        Directory.CreateDirectory(home);
        var probe = Path.Combine(home, ".write-" + Guid.NewGuid().ToString("N"));
        File.WriteAllText(probe, ""); File.Delete(probe);
    }
    static void PrepareHome(bool install) {
        string preferred = @"D:\SchoolSeatHelper";
        string fallback = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "SchoolSeatHelper");
        string home = SelectHome(preferred, fallback, EnsureWritable, out HomeNotice);
        ConfigPath = Path.Combine(home, "seat-helper.dat");
        // 公开入口不依赖设备密钥，尚未关联或助手故障也能打开原教学网页。
        File.WriteAllText(FallbackPath(), Text(Site, "platformUrl"), Encoding.UTF8);
        if (install) {
            var exe = Path.Combine(home, "SeatHelper.exe");
            CopyProgram(Application.ExecutablePath, exe);
            Install(exe, true);
        }
    }
    static void CopyProgram(string source, string destination) {
        if (string.Equals(Path.GetFullPath(source), Path.GetFullPath(destination), StringComparison.OrdinalIgnoreCase)) return;
        // 先完整复制再替换，复制失败时保留旧程序及关联文件。
        var staging = destination + "." + Guid.NewGuid().ToString("N") + ".tmp";
        try {
            File.Copy(source, staging, false);
            if (File.Exists(destination)) File.Replace(staging, destination, null);
            else File.Move(staging, destination);
        } finally { if (File.Exists(staging)) File.Delete(staging); }
    }
    static string RandomProof() {var bytes=new byte[32];using(var rng=new RNGCryptoServiceProvider())rng.GetBytes(bytes);return BitConverter.ToString(bytes).Replace("-","").ToLowerInvariant();}
    static void BeginAutomaticLink(bool open) {
        try {
            Site=Site??ReadSite();var proof=RandomProof();
            var data=Call("device/pair/start",new {fingerprint=Fingerprint(),localIp=LocalIp(),proof=proof},Site);
            Pending=new Dictionary<string,object>{{"token",Text(data,"token")},{"proof",proof},{"fingerprint",Fingerprint()}};
            PairUntil=DateTime.UtcNow.AddMinutes(5);Status="已获取内网IP，正在等待原网页登录关联";
            if(open)Process.Start(new ProcessStartInfo(PairUrl()){UseShellExecute=true});
        }catch {Pending=null;Status="自动关联暂不可用，网页可正常使用";if(open)Process.Start(new ProcessStartInfo(Text(Site,"platformUrl")){UseShellExecute=true});}
    }
    static string PairUrl() {return Text(Site,"platformUrl").TrimEnd('/')+"/student/index#seat-helper="+Text(Pending,"token");}
    static void PollPair() {
        if(Pending==null||Interlocked.Exchange(ref Busy,1)!=0)return;
        try {
            if(DateTime.UtcNow>PairUntil) {Pending=null;Status="关联等待已结束，打开平台可再次自动关联";return;}
            var request=Pending;var data=Call("device/pair/poll",request,Site);if(Text(data,"status")!="READY")return;
            var registration=(Dictionary<string,object>)data["registration"];
            registration["apiUrl"]=Text(Site,"apiUrl");registration["fingerprint"]=Fingerprint();
            if(!object.ReferenceEquals(Pending,request))return;
            var previous=Config;
            try {Config=registration;Save(ConfigPath);}catch {Config=previous;throw;}
            // 先落盘再确认领取；保存失败时保留短期结果，避免丢失设备密钥。
            try {Call("device/pair/ack",request,Site);}catch{}
            Pending=null;Status="机位已自动关联 · 内网IP："+LocalIp();LastPulse=DateTime.MinValue;
        }catch {Status="等待网页关联，普通上课不受影响";}
        finally {Interlocked.Exchange(ref Busy,0);}
    }
    static bool Configure() {
        using (var form = new Form { Text = "管理员配置机位助手（已有设备请先在后台撤销）", Width = 640, Height = 345, StartPosition = FormStartPosition.CenterScreen, FormBorderStyle = FormBorderStyle.FixedDialog, MaximizeBox = false }) {
            var api = new TextBox { Left=145, Top=25, Width=460, Text=Text(Config,"apiUrl") };
            var code = new TextBox { Left=145, Top=65, Width=410, UseSystemPasswordChar=true };
            var website = new TextBox { Left=145, Top=105, Width=460, Text=ReadFallback() };
            var folder = new TextBox { Left=145, Top=145, Width=410, Text = File.Exists(ConfigPath) ? Path.GetDirectoryName(ConfigPath) : Directory.Exists("D:\\") ? "D:\\SchoolSeatHelper" : Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),"SchoolSeatHelper") };
            folder.Enabled = !File.Exists(ConfigPath);
            var startup = new CheckBox { Left=145,Top=190,Width=380,Text="登录Windows后自动启动助手",Checked=true };
            form.Controls.AddRange(new Control[] { new Label { Left=15,Top=28,Text="平台接口地址",Width=125 }, api, new Label { Left=15,Top=68,Text="管理员提供的注册码",Width=125 }, code, new Label { Left=15,Top=108,Text="普通网页地址",Width=125 }, website, new Label { Left=15,Top=148,Text="安装目录",Width=125 }, folder, startup });
            var save = new Button { Left=350,Top=235,Width=200,Text="保存并创建桌面快捷方式" };
            form.Controls.Add(save); bool configured=false;
            save.Click += delegate {
                save.Enabled=false;
                try {
                    if (!string.IsNullOrWhiteSpace(website.Text)) ValidatePlatform(website.Text.Trim());
                    var nextConfig=Register(api.Text,code.Text);
                    var platform=Text(nextConfig,"platformUrl");
                    if (string.IsNullOrWhiteSpace(platform)) platform=website.Text.Trim();
                    ValidatePlatform(platform); nextConfig["platformUrl"]=platform;
                    Config=nextConfig;
                    var destination=Path.GetFullPath(folder.Text); Directory.CreateDirectory(destination);
                    var exe=Path.Combine(destination,"SeatHelper.exe"); var current=Application.ExecutablePath;
                    if(!string.Equals(exe,current,StringComparison.OrdinalIgnoreCase)) File.Copy(current,exe,true);
                    ConfigPath=Path.Combine(destination,"seat-helper.dat");
                    if (File.Exists(ConfigPath)) File.Copy(ConfigPath, ConfigPath+"."+DateTime.Now.ToString("yyyyMMddHHmmssfff")+".bak",false);
                    Save(ConfigPath); Install(exe,startup.Checked);
                    configured=true; form.Close();
                    if(!string.Equals(exe,current,StringComparison.OrdinalIgnoreCase)) { Process.Start(new ProcessStartInfo(exe,"--background") { WindowStyle=ProcessWindowStyle.Hidden }); TryOpen(); }
                } catch(Exception e) { MessageBox.Show(e.Message,"配置失败"); save.Enabled=true; }
            };
            form.ShowDialog();
            // 原下载目录的实例退出，后续始终运行已配置目录中的程序。
            return configured && string.Equals(Path.GetDirectoryName(ConfigPath),Path.GetDirectoryName(Application.ExecutablePath),StringComparison.OrdinalIgnoreCase);
        }
    }
    static InstallResult RunInstallSteps(Action shortcut, Action startup) {
        var result = new InstallResult();
        // 两项独立执行，桌面被限制时也应继续配置自启动。
        try { shortcut(); result.ShortcutReady = true; }
        catch { result.ShortcutError = "创建或核对失败，请检查桌面写入权限"; }
        try { startup(); result.StartupReady = true; }
        catch { result.StartupError = "写入或核对失败，请老师检查系统策略"; }
        return result;
    }
    static void CreateShortcut(string exe, string shortcutPath) {
        var shellType = Type.GetTypeFromProgID("WScript.Shell");
        object shell = null, shortcut = null;
        try {
            shell = Activator.CreateInstance(shellType);
            shortcut = shellType.InvokeMember("CreateShortcut", System.Reflection.BindingFlags.InvokeMethod, null, shell, new object[] { shortcutPath });
            var type = shortcut.GetType();
            type.InvokeMember("TargetPath", System.Reflection.BindingFlags.SetProperty, null, shortcut, new object[] { exe });
            type.InvokeMember("WorkingDirectory", System.Reflection.BindingFlags.SetProperty, null, shortcut, new object[] { Path.GetDirectoryName(exe) });
            type.InvokeMember("Save", System.Reflection.BindingFlags.InvokeMethod, null, shortcut, null);
            if (!File.Exists(shortcutPath)) throw new IOException("快捷方式未保存");
        } finally {
            if (shortcut != null) System.Runtime.InteropServices.Marshal.FinalReleaseComObject(shortcut);
            if (shell != null) System.Runtime.InteropServices.Marshal.FinalReleaseComObject(shell);
        }
    }
    static void WriteStartup(RegistryKey key, string exe, bool enabled) {
        string command = "\"" + exe + "\" --background";
        if (enabled) key.SetValue("SchoolSeatHelper", command);
        else key.DeleteValue("SchoolSeatHelper", false);
        key.Flush();
        if (enabled ? !string.Equals(Convert.ToString(key.GetValue("SchoolSeatHelper")), command, StringComparison.Ordinal) : key.GetValue("SchoolSeatHelper") != null)
            throw new IOException("自启动项核对失败");
    }
    static void Install(string exe, bool startup) {
        Installation = RunInstallSteps(
            delegate { CreateShortcut(exe, Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.DesktopDirectory), "学业测评平台.lnk")); },
            delegate {
                using (var key = Registry.CurrentUser.CreateSubKey(@"Software\Microsoft\Windows\CurrentVersion\Run")) WriteStartup(key, exe, startup);
            });
        if (!startup) Installation.StartupReady = false;
    }
    static string InstallationSummary() {
        var result = Installation ?? new InstallResult();
        return "安装位置：" + Path.GetDirectoryName(ConfigPath)
            + "\r\n运行位置：" + Application.ExecutablePath
            + "\r\n自动启动：" + (result.StartupReady ? "已设置（当前Windows用户登录后启动）" : "未完成；" + result.StartupError)
            + "\r\n桌面快捷方式：" + (result.ShortcutReady ? "已创建“学业测评平台”" : "未完成；" + result.ShortcutError)
            + (string.IsNullOrEmpty(HomeNotice) ? "" : "\r\n\r\n" + HomeNotice);
    }
    static void ShowStatusWindow() {
        if (StatusWindow != null && !StatusWindow.IsDisposed) { StatusWindow.Show(); StatusWindow.Activate(); return; }
        StatusWindow = CreateStatusWindow();
        StatusWindow.Show();
    }
    static Form CreateStatusWindow() {
        var form = new Form { Text = "学业测评机位助手 · 安装与运行状态", ClientSize = new System.Drawing.Size(640, 470), StartPosition = FormStartPosition.CenterScreen, FormBorderStyle = FormBorderStyle.FixedDialog, MaximizeBox = false, AutoScaleMode = AutoScaleMode.Dpi };
        var title = new Label { Left = 22, Top = 20, Width = 596, Height = 30, Font = new System.Drawing.Font("Microsoft YaHei", 13, System.Drawing.FontStyle.Bold) };
        var details = new TextBox { Left = 22, Top = 60, Width = 596, Height = 145, Multiline = true, ReadOnly = true, TabStop = false, ScrollBars = ScrollBars.Vertical, BorderStyle = BorderStyle.None, BackColor = form.BackColor };
        var live = new Label { Left = 22, Top = 220, Width = 596, Height = 60 };
        var baseline = new Label { Left = 22, Top = 290, Width = 596, Height = 90, ForeColor = System.Drawing.Color.DarkOrange, Text = "机房首次安装后，请老师保存还原基线：\r\n1. 完成网页关联，确认教师端能看到本机内网IP。\r\n2. 将程序目录、Windows自启动项及本机关联环境纳入还原基线。\r\n3. 重启还原后，检查助手自动启动及教师端连接。仅放D盘不能保证保留。" };
        var open = new Button { Left = 22, Top = 398, Width = 140, Height = 34, Text = "打开平台 / 关联" };
        var folder = new Button { Left = 172, Top = 398, Width = 120, Height = 34, Text = "打开安装目录" };
        var retry = new Button { Left = 302, Top = 398, Width = 130, Height = 34, Text = "重试自启动设置" };
        var hide = new Button { Left = 442, Top = 398, Width = 176, Height = 34, Text = "完成，后台运行" };
        Action refresh = delegate {
            title.Text = Installation != null && Installation.StartupReady && Installation.ShortcutReady ? "程序已安装，自启动已设置" : "程序已安装，部分设置需处理";
            var summary = InstallationSummary();
            if (details.Text != summary) { details.Text = summary; details.SelectionLength = 0; }
            live.Text = "内网IP：" + LocalIp() + "\r\n本机关联：" + (Config == null ? "尚未保存，请打开平台完成关联" : "已保存") + "\r\n运行状态：" + Status;
        };
        open.Click += delegate { ThreadPool.QueueUserWorkItem(delegate { TryOpen(); }); };
        folder.Click += delegate { try { Process.Start(new ProcessStartInfo(Path.GetDirectoryName(ConfigPath)) { UseShellExecute = true }); } catch { MessageBox.Show("无法打开目录，请根据上方路径手动查看。", "机位助手"); } };
        retry.Click += delegate { Install(Application.ExecutablePath, true); refresh(); };
        hide.Click += delegate { form.Hide(); Tray.ShowBalloonTip(3000, "机位助手正在后台运行", "可从右下角托盘查看安装与关联状态。", ToolTipIcon.Info); };
        form.FormClosing += delegate(object sender, FormClosingEventArgs e) {
            if (!Exiting && e.CloseReason == CloseReason.UserClosing) { e.Cancel = true; form.Hide(); }
        };
        var timer = new System.Windows.Forms.Timer { Interval = 1500 };
        timer.Tick += delegate { if (form.Visible) refresh(); };
        form.Disposed += delegate { timer.Dispose(); };
        form.Controls.AddRange(new Control[] { title, details, live, baseline, open, folder, retry, hide });
        form.Shown += delegate { hide.Focus(); };
        refresh(); timer.Start(); return form;
    }
    static void Pulse() {
        if(Interlocked.Exchange(ref Busy,1)!=0) return;
        try { var config=Config; Call("device/heartbeat",Auth(config),config); Status="已连接 · "+DateTime.Now.ToString("HH:mm:ss"); }
        catch { Status="连接中断或设备已撤销，网页可正常使用"; }
        finally { Interlocked.Exchange(ref Busy,0); }
    }
    static void TryOpen() {
        try {if(Config==null)BeginAutomaticLink(true);else OpenPlatform();}
        catch { Notify("未能打开平台，请管理员重新配置普通网页地址，或手动打开学校平台。"); }
    }
    static void Notify(string message) {
        if (Dispatcher != null && !Dispatcher.IsDisposed) {
            Dispatcher.BeginInvoke((Action)delegate { MessageBox.Show(message,"机位助手"); });
        } else MessageBox.Show(message,"机位助手");
    }
    static string ResolvePlatformUrl() {
        try {
            var config=Config;
            var data=Call("device/open",Auth(config),config); var url=Text(data,"url");
            var uri=new Uri(url); ValidatePlatform(uri.GetLeftPart(UriPartial.Authority));
            // 只保存公开站点根地址，不把单次关联凭证写入明文配置。
            try { File.WriteAllText(FallbackPath(),uri.GetLeftPart(UriPartial.Authority),Encoding.UTF8); } catch { }
            Status="已关联设备，正在打开平台";
            return url;
        } catch {
            var fallback=ReadFallback();
            if (string.IsNullOrEmpty(fallback)) throw new Exception("缺少普通网页地址");
            Status="机位未关联，已使用普通网页入口";
            return fallback;
        }
    }
    static void OpenPlatform() {
        Process.Start(new ProcessStartInfo(ResolvePlatformUrl()) { UseShellExecute=true });
    }
    static string FallbackPath() { return Path.Combine(Path.GetDirectoryName(ConfigPath),"platform-url.txt"); }
    static string ReadFallback() {
        try {
            var value=File.Exists(FallbackPath()) ? File.ReadAllText(FallbackPath(),Encoding.UTF8).Trim() : Text(Config,"platformUrl");
            ValidatePlatform(value); return value;
        } catch { return ""; }
    }
    static void ValidatePlatform(string value) {
        ValidateApi(value); var uri=new Uri(value);
        if (uri.AbsolutePath!="/") throw new Exception("普通网页地址必须为HTTP或HTTPS站点根地址，不含接口路径");
    }
    static Dictionary<string,object> Auth(Dictionary<string,object> config=null) {
        config=config??Config;
        return new Dictionary<string,object> { {"deviceId",Text(config,"deviceId")},{"secret",Text(config,"secret")},{"fingerprint",Fingerprint()},{"localIp",LocalIp()} };
    }
    static Dictionary<string,object> Call(string path,object payload,Dictionary<string,object> config=null) {
        var api=Text(config??Config,"apiUrl"); ValidateApi(api);
        using(var client=new TimedWebClient()) {
            client.Headers[HttpRequestHeader.ContentType]="application/json;charset=utf-8";
            var response=client.UploadData(api.TrimEnd('/')+"/business/seat-helper/"+path,"POST",Encoding.UTF8.GetBytes(Json.Serialize(payload)));
            var root=Json.Deserialize<Dictionary<string,object>>(Encoding.UTF8.GetString(response));
            if(Convert.ToInt32(root["code"])!=200) throw new Exception(Text(root,"msg"));
            return root.ContainsKey("data") && root["data"] is Dictionary<string,object> ? (Dictionary<string,object>)root["data"] : new Dictionary<string,object>();
        }
    }
    // 允许学校现有的内网 HTTP 平台；地址仍不能包含账号、查询参数或片段。
    static void ValidateApi(string value) { Uri uri; if(!Uri.TryCreate(value,UriKind.Absolute,out uri) || string.IsNullOrEmpty(uri.Host) || uri.UserInfo!="" || uri.Query!="" || uri.Fragment!="" || uri.Port==0 || !(uri.Scheme=="https" || uri.Scheme=="http")) throw new Exception("接口地址需使用HTTP或HTTPS，例如http://10.52.1.130/prod-api。"); }
    static void Save(string path) { var platform=Text(Config,"platformUrl"); if (!string.IsNullOrEmpty(platform)) { ValidatePlatform(platform); File.WriteAllText(Path.Combine(Path.GetDirectoryName(path),"platform-url.txt"),platform,Encoding.UTF8); } var bytes=Encoding.UTF8.GetBytes(Json.Serialize(Config)); File.WriteAllBytes(path,ProtectedData.Protect(bytes,Encoding.UTF8.GetBytes(Fingerprint()),DataProtectionScope.LocalMachine)); }
    static Dictionary<string,object> Read() { var bytes=ProtectedData.Unprotect(File.ReadAllBytes(ConfigPath),Encoding.UTF8.GetBytes(Fingerprint()),DataProtectionScope.LocalMachine); return Json.Deserialize<Dictionary<string,object>>(Encoding.UTF8.GetString(bytes)); }
    static string Fingerprint() {
        string guid=Convert.ToString(Registry.GetValue("HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Cryptography","MachineGuid","")); string uuid="";
        try { using(var query=new ManagementObjectSearcher("SELECT UUID FROM Win32_ComputerSystemProduct")) foreach(ManagementObject obj in query.Get()) { uuid=Convert.ToString(obj["UUID"]); break; } } catch { }
        using(var sha=SHA256.Create()) return BitConverter.ToString(sha.ComputeHash(Encoding.UTF8.GetBytes(guid+"|"+uuid))).Replace("-","").ToLowerInvariant();
    }
    static bool PrivateIp(IPAddress ip) { var b=ip.GetAddressBytes(); return b.Length==4 && (b[0]==10 || b[0]==192&&b[1]==168 || b[0]==172&&b[1]>=16&&b[1]<=31); }
    static string LocalIp() {
        var interfaces=NetworkInterface.GetAllNetworkInterfaces().Where(n=>n.OperationalStatus==OperationalStatus.Up && (n.NetworkInterfaceType==NetworkInterfaceType.Ethernet || n.NetworkInterfaceType==NetworkInterfaceType.Wireless80211)).OrderByDescending(n=>n.GetIPProperties().GatewayAddresses.Count>0);
        foreach(var adapter in interfaces) foreach(var address in adapter.GetIPProperties().UnicastAddresses) if(PrivateIp(address.Address)) return address.Address.ToString();
        return "";
    }
    static string Text(Dictionary<string,object> data,string key) { object value; return data!=null && data.TryGetValue(key,out value) && value!=null ? Convert.ToString(value) : ""; }
    class TimedWebClient:WebClient { protected override WebRequest GetWebRequest(Uri address) { var request=base.GetWebRequest(address); request.Timeout=10000; return request; } }
}
