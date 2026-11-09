"""保留核心文档旧发布原文，并收口本轮经过验证的本机状态。"""
import re
from migrate_local import ROOT
core=ROOT/'contexts/PROJECT_CORE.md'
text=core.read_text(encoding='utf-8')
if '## 本机已实现未发布：选择题图片增强 A' not in text:
    archive=ROOT/'contexts/PROJECT_HISTORY_20261106_before_choice_images.md'
    if archive.exists():assert archive.read_text(encoding='utf-8')==text,'归档已存在且内容不同，停止覆盖'
    else:archive.write_text(text,encoding='utf-8')
    start=text.index('## 已上线1.30.73：');end=text.index('## 当前焦点：2025 宁波优秀课例批次')
    replacement='''## 历史发布 1.30.72/73 与题库工作台

- 1.30.73 修复课程推进后 Python 判题终态，1.30.72 包含题库工作台、权限审查、PY 作品和成绩按合计排名；当前正式以顶部 1.30.74 为准。
- 1.30.72 曾因均分/合计不一致回退，后经线上逐人核对收敛；原始证据、未闭环批改/跨校风险和当时验证边界完整保留在 [2026-11-06 精简前原文](PROJECT_HISTORY_20261106_before_choice_images.md)。
- 专题入口：[1.30.73](class-readiness-20261105.md)、[题库工作台](question-workbench/tasks.md)、[PY 作品](practical-python-upload/tasks.md)、[发布日志](RELEASE_LOG.md)。

'''
    text=text[:start]+replacement+text[end:]
    status='''## 本机已实现未发布：选择题图片增强 A（2026-11-06）

- 用户已确认方案并授权开发。题干3图/选项1图、上传/粘贴/拖拽、自动另存保留历史、保存回执找回；Excel同格图文预览/修正/原子确认/导出回导，普通课/错题/学情与抽测媒体透传，A—D评分保持。没有统一版本系统或新依赖。
- 本机已备份后执行`sql/choice_question_media_v1.sql`（两列四表），私有原图在profile同级`<profile名>-question-images`，不可置于公开映射。生产未连接/迁移/部署；未来需独立备份、持久目录/权限核对、前后端同步发布并重启后端，不能直接回退到不识图旧程序。
- 验证：后端933项（3跳过）/媒体定向22项、前端构建40项门禁/媒体3项通过；真实50题76图片、并发确认/中途失败整批回滚、丢响应回执恢复、学生真实10/0/10分、错题/个人卷权限、桌面390/320及组卷预览通过。临时数据已清理，原1738题/4187快照逐行不变；前端全仓220通过、1个既有Python测试因`@/api`别名加载失败未越界改。
- 待验：教师实际Excel/WPS新式图片变体、机房压力和七天清理观察。当前4178/18080为独立本机审阅实例，8080未替换；工作区还有其他在研修改，整包不可直接发布。[验收与截图](../docs/choice-question-images-development-verification-20261106.md)、[设计/任务](choice-question-media/tasks.md)。

'''
    first=text.index('## ');text=text[:first]+status+text[first:]
    text=re.sub(r'上下文版本 \*\*v(\d+)\.(\d+)\*\*',lambda m:f'上下文版本 **v{m[1]}.{int(m[2])+1}**',text,count=1)
    core.write_text(text,encoding='utf-8')
print('核心上下文已收口，行数',len(text.splitlines()))
