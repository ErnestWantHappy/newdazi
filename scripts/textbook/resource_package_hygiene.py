"""教学配套包净化：保留教学源码，剔除运行环境和本地数据。"""
import copy,io,re,subprocess,tempfile,zipfile
from pathlib import Path

def forbidden(name):
    parts = name.replace('\\','/').lower().split('/')
    base=parts[-1]
    return (any(p in {'.git','.venv','venv','node_modules','__pycache__','.pytest_cache'} for p in parts)
            or base=='.env' or (base.startswith('.env.') and base not in {'.env.example','.env.sample','.env.template'})
            or bool(re.search(r'\.(?:db|sqlite|sqlite3)(?:-wal|-shm|-journal)?$',base))
            or Path(base).suffix in {'.log','.msi','.exe','.pyc'})

def clean_zip(data, prefix='', depth=0):
    if depth>8: raise RuntimeError('嵌套包深度超过审计上限')
    removed=[]; changes={}
    with zipfile.ZipFile(io.BytesIO(data)) as z:
        for info in z.infolist():
            if forbidden(info.filename):
                removed.append(prefix+info.filename); changes[info.filename]=None
            elif info.filename.lower().endswith('.zip') and not info.is_dir():
                nested=z.read(info)
                new, rm=clean_zip(nested,prefix+info.filename+'!/',depth+1)
                if rm: changes[info.filename]=new; removed.extend(rm)
            elif info.filename.lower().endswith(('.7z','.rar')):
                nested, rm=clean_other(z.read(info),prefix+info.filename+'!/',depth+1)
                if rm: changes[info.filename]=(nested,True); removed.extend(rm)
        if not changes: return data,[]
        buf=io.BytesIO()
        with zipfile.ZipFile(buf,'w',allowZip64=True) as dest:
            for info in z.infolist():
                if info.filename in changes and changes[info.filename] is None: continue
                content=changes.get(info.filename)
                if isinstance(content,tuple):
                    info=copy.copy(info); info.filename=str(Path(info.filename).with_suffix('.zip')).replace('\\','/')
                    content=content[0]
                dest.writestr(info,content if content is not None else z.read(info))
            dest.writestr('平台资料整理说明.txt','本包已移除本地环境配置、运行数据库、日志、安装程序和开发缓存。\n如需运行项目，请按原说明安装依赖并建立自己的本地数据；原教学源码与素材保留。\n')
    return buf.getvalue(),removed

def clean_other(data,prefix,depth):
    with tempfile.TemporaryDirectory(prefix='teaching-audit-') as tmp:
        base=Path(tmp); archive=base/'input.rar'; folder=base/'content'
        archive.write_bytes(data)
        tool=r'C:\Program Files\NVIDIA Corporation\NVIDIA GeForce Experience\7z.exe'
        result=subprocess.run([tool,'x',str(archive),'-o'+str(folder),'-y'],capture_output=True)
        if result.returncode: raise RuntimeError('无法审查压缩包：'+prefix)
        buf=io.BytesIO()
        with zipfile.ZipFile(buf,'w',zipfile.ZIP_DEFLATED) as z:
            for path in folder.rglob('*'):
                if path.is_file(): z.write(path,path.relative_to(folder).as_posix())
        return clean_zip(buf.getvalue(),prefix,depth)

