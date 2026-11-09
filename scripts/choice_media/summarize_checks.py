import json
import xml.etree.ElementTree as ET
from migrate_local import ROOT
modules={}
for module in ['ruoyi-common','ruoyi-system','ruoyi-framework','ruoyi-business']:
    result={'tests':0,'failures':0,'errors':0,'skipped':0}
    for file in (ROOT/'RuoYi-Vue'/module/'target/surefire-reports').glob('TEST-*.xml'):
        data=ET.parse(file).getroot().attrib
        for key in result:result[key]+=int(data.get(key,0))
    modules[module]=result
summary={'modules':modules,'feature':{}}
for file in (ROOT/'RuoYi-Vue/ruoyi-business/target/surefire-reports').glob('TEST-*Question*Test.xml'):
    if any(name in file.name for name in ['QuestionMediaCodecTest','QuestionWorkbookReaderTest','QuestionSaveServiceTest']):
        data=ET.parse(file).getroot().attrib;summary['feature'][data['name']]={key:int(data.get(key,0)) for key in ['tests','failures','errors','skipped']}
(ROOT/'output/choice-media/test-summary.json').write_text(json.dumps(summary,indent=2),encoding='utf-8')
print(json.dumps(summary))
