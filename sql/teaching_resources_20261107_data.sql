-- 电子教材与教学资源 v1 数据导入（2026-11-07，自动生成）
-- 依赖 teaching_resources_20261107_v1.sql 先建表。幂等：按 source_key / resource_key / grade / lesson_no 更新。
-- 不修改旧课例的作者、身份、评价和既有资源。

SET NAMES utf8mb4;
START TRANSACTION;

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026a40eb5ab3bcbc1834c60ba98c395118b',b.book_id,'lesson-1','感知智能生活','施益佳','慈溪市庵东镇西二小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-1' AND s.kind='LESSON'
WHERE b.book_code='xxkj-3-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'7bc381e63acdac570cee97888a154d6f098a8a88e763b7201fa41b8123e69488','DESIGN','1《感知智能生活》施益佳','1《感知智能生活》施益佳.docx','7b/7bc381e63acdac570cee97888a154d6f098a8a88e763b7201fa41b8123e69488.docx','previews/7bc381e63acdac570cee97888a154d6f098a8a88e763b7201fa41b8123e69488.pdf',NULL,NULL,0,159044,'来源：三上（前湾高新）/1《感知智能生活》施益佳.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026a40eb5ab3bcbc1834c60ba98c395118b'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-7208a451fc3aec289cf53c40550eabcdb677a474a7152331b33f578a027d7114','MATERIAL','寻找智能“小眼睛”-高新区-第3课《寻找智能“小眼睛”》(1)','第3课-寻找智能“小眼睛”-黄意-高新区-第3课《寻找智能“小眼睛”》(1).zip','72/7208a451fc3aec289cf53c40550eabcdb677a474a7152331b33f578a027d7114.zip',NULL,NULL,NULL,0,2765530,'共 5 个文件，来源目录：三上（前湾高新）/高新区-第3课《寻找智能“小眼睛”》(1)','解压后用浏览器打开入口 HTML；若页面依赖本地服务或模型文件，请按包内 README 启动。',1
FROM biz_textbook_case c WHERE c.source_key='3005d1f6068aae767065034b133c7ca1'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-1fc2fffc0e47677be05ec8aff304702525147167917af0174e39081cd3fc4b29','MATERIAL','触摸智能“小耳朵”-宁波市-第4课《触摸智能小耳朵》','第4课-触摸智能“小耳朵”-罗洁-宁波市-第4课《触摸智能小耳朵》.zip','1f/1fc2fffc0e47677be05ec8aff304702525147167917af0174e39081cd3fc4b29.zip',NULL,NULL,NULL,0,1032,'共 1 个文件，来源目录：三上（前湾高新）/宁波市-第4课《触摸智能小耳朵》','解压后按文件名和包内 README 使用。',1
FROM biz_textbook_case c WHERE c.source_key='f19511deadab8e9a2461c8f4dbfe2da8'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb202629ef645c9f8c95528194d466799d018e',b.book_id,'lesson-6','进入在线平台','丁超 宁波杭州','慈溪市崇寿镇中心小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-6' AND s.kind='LESSON'
WHERE b.book_code='xxkj-3-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'7b757227c32e8e03c28fd33d0771e889914a63f9e9353947bd9ab325bfa96fda','DESIGN','6《进入在线平台》丁超 陈奕丹','6《进入在线平台》丁超 陈奕丹.docx','7b/7b757227c32e8e03c28fd33d0771e889914a63f9e9353947bd9ab325bfa96fda.docx','previews/7b757227c32e8e03c28fd33d0771e889914a63f9e9353947bd9ab325bfa96fda.pdf',NULL,NULL,0,143693,'来源：三上（前湾高新）/6《进入在线平台》丁超 陈奕丹.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb202629ef645c9f8c95528194d466799d018e'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb20267a6fbb54c05f4ca8bc556dde265b0729',b.book_id,'lesson-7','下载平台资源','谢健辉','宁波前湾新区滨海小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-7' AND s.kind='LESSON'
WHERE b.book_code='xxkj-3-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'26f16e03572dd34829ea96bcb7f9dbe50b37ef3d95e1e90f3a5c95ee0dd6ec16','DESIGN','7《下载平台资源》谢健辉','7《下载平台资源》谢健辉.docx','26/26f16e03572dd34829ea96bcb7f9dbe50b37ef3d95e1e90f3a5c95ee0dd6ec16.docx','previews/26f16e03572dd34829ea96bcb7f9dbe50b37ef3d95e1e90f3a5c95ee0dd6ec16.pdf',NULL,NULL,0,188976,'来源：三上（前湾高新）/7《下载平台资源》谢健辉.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb20267a6fbb54c05f4ca8bc556dde265b0729'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb202687d5ff5ee59428742a31fc31b9661126',b.book_id,'lesson-8','查看资源与文件','倪双双 王诗莹','宁波前湾新区滨海小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-8' AND s.kind='LESSON'
WHERE b.book_code='xxkj-3-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'936be18fc373d17384bce2b450980a29d94663c230f984a130021a0a595465a1','DESIGN','8《查看资源与文件》倪双双 王诗莹','8《查看资源与文件》倪双双 王诗莹.docx','93/936be18fc373d17384bce2b450980a29d94663c230f984a130021a0a595465a1.docx','previews/936be18fc373d17384bce2b450980a29d94663c230f984a130021a0a595465a1.pdf',NULL,NULL,0,417741,'来源：三上（前湾高新）/8《查看资源与文件》倪双双 王诗莹.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb202687d5ff5ee59428742a31fc31b9661126'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026f8ae0270ee3c333db60691f432bd311a',b.book_id,'lesson-9','分类整理资源','陈桂琳','宁波前湾新区世纪城实验小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-9' AND s.kind='LESSON'
WHERE b.book_code='xxkj-3-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'d5fedd038af89ac9a78bf71d63511010b0e1bc510a4268f19d90f5eaeecbda79','DESIGN','9《分类整理资源》陈桂琳','9《分类整理资源》陈桂琳.docx','d5/d5fedd038af89ac9a78bf71d63511010b0e1bc510a4268f19d90f5eaeecbda79.docx','previews/d5fedd038af89ac9a78bf71d63511010b0e1bc510a4268f19d90f5eaeecbda79.pdf',NULL,NULL,0,589463,'来源：三上（前湾高新）/9《分类整理资源》陈桂琳.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026f8ae0270ee3c333db60691f432bd311a'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026d5a428e778bc1782e06fcbc2d7138b2e',b.book_id,'lesson-10','共享资源途径','张楚人','宁波前湾新区海韵小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-10' AND s.kind='LESSON'
WHERE b.book_code='xxkj-3-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'0232a7b0b1cb7a5074aac1563cfbecde52a96fd3d9cf4c555ccc8d78f6e22184','DESIGN','10《共享资源途径》张楚人','10《共享资源途径》张楚人.docx','02/0232a7b0b1cb7a5074aac1563cfbecde52a96fd3d9cf4c555ccc8d78f6e22184.docx','previews/0232a7b0b1cb7a5074aac1563cfbecde52a96fd3d9cf4c555ccc8d78f6e22184.pdf',NULL,NULL,0,344981,'来源：三上（前湾高新）/10《共享资源途径》张楚人.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026d5a428e778bc1782e06fcbc2d7138b2e'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026042774284524d176bc1ecca028ce61c7',b.book_id,'lesson-11','认识在线社会','施益佳','慈溪市庵东镇西二小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-11' AND s.kind='LESSON'
WHERE b.book_code='xxkj-3-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'84f821bc62d0a83bdae2428f52c688aaada882f5aeafb064317aa95c8bab69c7','DESIGN','11《认识在线社会》施益佳','11《认识在线社会》施益佳.docx','84/84f821bc62d0a83bdae2428f52c688aaada882f5aeafb064317aa95c8bab69c7.docx','previews/84f821bc62d0a83bdae2428f52c688aaada882f5aeafb064317aa95c8bab69c7.pdf',NULL,NULL,0,157398,'来源：三上（前湾高新）/11《认识在线社会》施益佳.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026042774284524d176bc1ecca028ce61c7'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb20263484e2bbed01d4f194becec42ed0fa51',b.book_id,'lesson-12','体验在线应用','朱佰文','宁波高新区实验学校','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-12' AND s.kind='LESSON'
WHERE b.book_code='xxkj-3-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'ab6fbbb13d66ecfcbd477e41e1ebde4b3a3fa6056bfc0508406fac5d7b7e2662','DESIGN','12《 体验在线应用》教学设计','12《 体验在线应用》教学设计.docx','ab/ab6fbbb13d66ecfcbd477e41e1ebde4b3a3fa6056bfc0508406fac5d7b7e2662.docx','previews/ab6fbbb13d66ecfcbd477e41e1ebde4b3a3fa6056bfc0508406fac5d7b7e2662.pdf',NULL,NULL,0,52871,'来源：三上（前湾高新）/12《 体验在线应用》教学设计.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb20263484e2bbed01d4f194becec42ed0fa51'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb20264992b1092b222c31512331a1b8079c48',b.book_id,'lesson-13','绘制在线生活','贺佳唯','宁波高新区外国语学校','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-13' AND s.kind='LESSON'
WHERE b.book_code='xxkj-3-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'9b17e97a2ff2edbb1cb25889b1aea4ea77124bac1ba5c57eec5521f283d4dcac','DESIGN','13《绘制在线生活》教学设计','13《绘制在线生活》教学设计.docx','9b/9b17e97a2ff2edbb1cb25889b1aea4ea77124bac1ba5c57eec5521f283d4dcac.docx','previews/9b17e97a2ff2edbb1cb25889b1aea4ea77124bac1ba5c57eec5521f283d4dcac.pdf',NULL,NULL,0,22164,'来源：三上（前湾高新）/13《绘制在线生活》教学设计.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb20264992b1092b222c31512331a1b8079c48'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb202676ede82e963542287d0b52398f966282',b.book_id,'lesson-14','关注网络痕迹','梁健飞','宁波高新区实验学校','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-14' AND s.kind='LESSON'
WHERE b.book_code='xxkj-3-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'976596df0faaf23b5ece8b582e48c0c116d5c04e683fe37449e7de7e5370af67','DESIGN','14 《关注网络痕迹》教学设计','14 《关注网络痕迹》教学设计.docx','97/976596df0faaf23b5ece8b582e48c0c116d5c04e683fe37449e7de7e5370af67.docx','previews/976596df0faaf23b5ece8b582e48c0c116d5c04e683fe37449e7de7e5370af67.pdf',NULL,NULL,0,88840,'来源：三上（前湾高新）/14 《关注网络痕迹》教学设计.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb202676ede82e963542287d0b52398f966282'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026a5728a7d7405965cc028adaac73627c6',b.book_id,'lesson-15','保护数字身份','','','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-15' AND s.kind='LESSON'
WHERE b.book_code='xxkj-3-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'7ceba550c5e0a2f1b748677411b2789e8fc9e50c9d5b45521e61bf1fb3ea7eb1','DESIGN','15《 保护数字身份》教学设计','15《 保护数字身份》教学设计.docx','7c/7ceba550c5e0a2f1b748677411b2789e8fc9e50c9d5b45521e61bf1fb3ea7eb1.docx','previews/7ceba550c5e0a2f1b748677411b2789e8fc9e50c9d5b45521e61bf1fb3ea7eb1.pdf',NULL,NULL,0,23717,'来源：三上（前湾高新）/15《 保护数字身份》教学设计.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026a5728a7d7405965cc028adaac73627c6'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026b9e434afd59ee68e9518dd4ce933bb7c',b.book_id,'lesson-16','分解问题步骤','','','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-16' AND s.kind='LESSON'
WHERE b.book_code='xxkj-3-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'7e486c9ebb977c8a07831fcca9b876fae27cbbe41b6379797a3e1c30a760d7c8','DESIGN','16《分解问题步骤》教学设计','16《分解问题步骤》教学设计.docx','7e/7e486c9ebb977c8a07831fcca9b876fae27cbbe41b6379797a3e1c30a760d7c8.docx','previews/7e486c9ebb977c8a07831fcca9b876fae27cbbe41b6379797a3e1c30a760d7c8.pdf',NULL,NULL,0,238074,'来源：三上（前湾高新）/16《分解问题步骤》教学设计.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026b9e434afd59ee68e9518dd4ce933bb7c'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026147476e98639197e008242c187a0cb72',b.book_id,'lesson-17','在线协作学习','吴芳芳','宁波高新区新明中心小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-17' AND s.kind='LESSON'
WHERE b.book_code='xxkj-3-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'48e7463314afd855b26531e069c127c2826a1089a25a6187f1451449d76e3652','DESIGN','17《在线协作学习》教学设计','17《在线协作学习》教学设计.docx','48/48e7463314afd855b26531e069c127c2826a1089a25a6187f1451449d76e3652.docx','previews/48e7463314afd855b26531e069c127c2826a1089a25a6187f1451449d76e3652.pdf',NULL,NULL,0,20992,'来源：三上（前湾高新）/17《在线协作学习》教学设计.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026147476e98639197e008242c187a0cb72'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026268785e053145f008178fd8744c76eed',b.book_id,'lesson-18','分享学习成果','周冰玲','浙江师范大学附属宁波高新区求精书院','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-18' AND s.kind='LESSON'
WHERE b.book_code='xxkj-3-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'cda161cc7376a7e1eaca6d684546278ae3c61be0fb55a4947735e69591069b17','DESIGN','18《 分享学习成果》教学设计','18《 分享学习成果》教学设计.docx','cd/cda161cc7376a7e1eaca6d684546278ae3c61be0fb55a4947735e69591069b17.docx','previews/cda161cc7376a7e1eaca6d684546278ae3c61be0fb55a4947735e69591069b17.pdf',NULL,NULL,0,251635,'来源：三上（前湾高新）/18《 分享学习成果》教学设计.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026268785e053145f008178fd8744c76eed'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026d756c7b7b77b6fd0769a7a499ad4d3c9',b.book_id,'lesson-1','多样的数字资源','王蕙兰','宁波高新区实验学校','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-1' AND s.kind='LESSON'
WHERE b.book_code='xxkj-3-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'1efa48a427cde2917a5331f6735ba1893a024b04dbf29578e5667f2bec621952','DESIGN','第1课  《多样的数字资源》教学设计','第1课  《多样的数字资源》教学设计.docx','1e/1efa48a427cde2917a5331f6735ba1893a024b04dbf29578e5667f2bec621952.docx','previews/1efa48a427cde2917a5331f6735ba1893a024b04dbf29578e5667f2bec621952.pdf',NULL,NULL,0,53779,'来源：三下（高新前湾）/第1课  《多样的数字资源》教学设计.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026d756c7b7b77b6fd0769a7a499ad4d3c9'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026ac8212f3f0816d5009d6260439d03167',b.book_id,'lesson-2','感知媒体编码','','','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-2' AND s.kind='LESSON'
WHERE b.book_code='xxkj-3-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'4eb877b6a447cb1d827c77ac9e2bbcce614e9f71fa81af7ee36f8daf61c61c42','DESIGN','第2课《感知媒体编码》教学设计','第2课《感知媒体编码》教学设计.docx','4e/4eb877b6a447cb1d827c77ac9e2bbcce614e9f71fa81af7ee36f8daf61c61c42.docx','previews/4eb877b6a447cb1d827c77ac9e2bbcce614e9f71fa81af7ee36f8daf61c61c42.pdf',NULL,NULL,0,22085,'来源：三下（高新前湾）/第2课《感知媒体编码》教学设计.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026ac8212f3f0816d5009d6260439d03167'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026c8d099c3a9aef29be6b680899e453005',b.book_id,'lesson-3','媒体文件类型','分析','','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-3' AND s.kind='LESSON'
WHERE b.book_code='xxkj-3-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'5ad6fff8452f3b0a546ceea5d4d4259b6051eeaa82f670fd5a0e39a8256fd27f','VIDEO','拓展视频一','拓展视频一.mp4','5a/5ad6fff8452f3b0a546ceea5d4d4259b6051eeaa82f670fd5a0e39a8256fd27f.mp4','5a/5ad6fff8452f3b0a546ceea5d4d4259b6051eeaa82f670fd5a0e39a8256fd27f.mp4',NULL,NULL,0,15250736,'来源：三下（高新前湾）/第3课 认识媒体类型/拓展视频一.mp4','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026c8d099c3a9aef29be6b680899e453005'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'d0aae4ae7cc7bce0e21a6a9452fa316dbc12760c808a50abd38458a95663d9a0','VIDEO','拓展视频二','拓展视频二.mp4','d0/d0aae4ae7cc7bce0e21a6a9452fa316dbc12760c808a50abd38458a95663d9a0.mp4','d0/d0aae4ae7cc7bce0e21a6a9452fa316dbc12760c808a50abd38458a95663d9a0.mp4',NULL,NULL,0,8662041,'来源：三下（高新前湾）/第3课 认识媒体类型/拓展视频二.mp4','',2
FROM biz_textbook_case c WHERE c.source_key='nb2026c8d099c3a9aef29be6b680899e453005'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'5b2fbca799f90526155befbcd344f01642521cbe99cddcb93f1d733a80959190','MATERIAL','第三课  认识媒体类型','第三课  认识媒体类型.doc','5b/5b2fbca799f90526155befbcd344f01642521cbe99cddcb93f1d733a80959190.doc','previews/5b2fbca799f90526155befbcd344f01642521cbe99cddcb93f1d733a80959190.pdf',NULL,NULL,0,60071,'来源：三下（高新前湾）/第3课 认识媒体类型/第三课  认识媒体类型.doc','',3
FROM biz_textbook_case c WHERE c.source_key='nb2026c8d099c3a9aef29be6b680899e453005'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'b79639d01a579c7220091fd096d94daac197882de02205c1b3cdd8b507145d42','MATERIAL','第三课 认识媒体类型-同步练习','第三课 认识媒体类型-同步练习.docx','b7/b79639d01a579c7220091fd096d94daac197882de02205c1b3cdd8b507145d42.docx','previews/b79639d01a579c7220091fd096d94daac197882de02205c1b3cdd8b507145d42.pdf',NULL,NULL,0,14379,'来源：三下（高新前湾）/第3课 认识媒体类型/第三课 认识媒体类型-同步练习.docx','',4
FROM biz_textbook_case c WHERE c.source_key='nb2026c8d099c3a9aef29be6b680899e453005'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'3c3332230a23cb592b77635b86060cecc12d6bb6b0cd60ba23061b256656233a','SLIDES','第三课 认识媒体类型','第三课 认识媒体类型.pptx','3c/3c3332230a23cb592b77635b86060cecc12d6bb6b0cd60ba23061b256656233a.pptx','previews/3c3332230a23cb592b77635b86060cecc12d6bb6b0cd60ba23061b256656233a.pdf',NULL,NULL,0,52230525,'来源：三下（高新前湾）/第3课 认识媒体类型/第三课 认识媒体类型.pptx','',5
FROM biz_textbook_case c WHERE c.source_key='nb2026c8d099c3a9aef29be6b680899e453005'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'d3f5c33a3953ecbc9a82fde1c0038bc1952734795c3aac50f30d72f4eb6735d3','VIDEO','课堂导入视频','课堂导入视频.mp4','d3/d3f5c33a3953ecbc9a82fde1c0038bc1952734795c3aac50f30d72f4eb6735d3.mp4','d3/d3f5c33a3953ecbc9a82fde1c0038bc1952734795c3aac50f30d72f4eb6735d3.mp4',NULL,NULL,0,25142335,'来源：三下（高新前湾）/第3课 认识媒体类型/课堂导入视频.mp4','',6
FROM biz_textbook_case c WHERE c.source_key='nb2026c8d099c3a9aef29be6b680899e453005'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026a244314295083944abbcc69da6c42e03',b.book_id,'lesson-3','媒体文件类型','教材','','NINGBO2026','PUBLISHED',102
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-3' AND s.kind='LESSON'
WHERE b.book_code='xxkj-3-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'5b2fbca799f90526155befbcd344f01642521cbe99cddcb93f1d733a80959190','DESIGN','第3课 《 认识媒体类型》教学设计','第3课 《 认识媒体类型》教学设计.doc','5b/5b2fbca799f90526155befbcd344f01642521cbe99cddcb93f1d733a80959190.doc','previews/5b2fbca799f90526155befbcd344f01642521cbe99cddcb93f1d733a80959190.pdf',NULL,NULL,0,60071,'来源：三下（高新前湾）/第3课 《 认识媒体类型》教学设计.doc','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026a244314295083944abbcc69da6c42e03'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb20269840dda448dd6e30e9d6a8ef770fffc1',b.book_id,'lesson-4','数字资源分类','','','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-4' AND s.kind='LESSON'
WHERE b.book_code='xxkj-3-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'133040c2f8526826b9fcf8be1df5cc7bf229f53bf0f21eed2cc91b1c1bfafd8b','DESIGN','第4课《数字资源分类》教学设计','第4课《数字资源分类》教学设计.docx','13/133040c2f8526826b9fcf8be1df5cc7bf229f53bf0f21eed2cc91b1c1bfafd8b.docx','previews/133040c2f8526826b9fcf8be1df5cc7bf229f53bf0f21eed2cc91b1c1bfafd8b.pdf',NULL,NULL,0,22882,'来源：三下（高新前湾）/第4课《数字资源分类》教学设计.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb20269840dda448dd6e30e9d6a8ef770fffc1'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb202601d896e5a4043a12071aff1c9e60df5d',b.book_id,'lesson-5','应用数字资源','','','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-5' AND s.kind='LESSON'
WHERE b.book_code='xxkj-3-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'721e9cccebaa45b0a03d8fe4a53ecf44cabab91c7dcf1afb6406cc5fac5625f1','DESIGN','第5课《应用数字资源》教学设计','第5课《应用数字资源》教学设计.docx','72/721e9cccebaa45b0a03d8fe4a53ecf44cabab91c7dcf1afb6406cc5fac5625f1.docx','previews/721e9cccebaa45b0a03d8fe4a53ecf44cabab91c7dcf1afb6406cc5fac5625f1.pdf',NULL,NULL,0,22922,'来源：三下（高新前湾）/第5课《应用数字资源》教学设计.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb202601d896e5a4043a12071aff1c9e60df5d'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb20264064a97a4b50134c7bffe20ea9fd1086',b.book_id,'lesson-6','数字作品面面观','周冰玲','宁波高新区求精书院','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-6' AND s.kind='LESSON'
WHERE b.book_code='xxkj-3-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'a3df98ae9503374fa45e77e6959c53b4ae8f602c315699ec6b9c0b486e50da06','DESIGN','第6课 《数字作品面面观》教学设计','第6课 《数字作品面面观》教学设计.docx','a3/a3df98ae9503374fa45e77e6959c53b4ae8f602c315699ec6b9c0b486e50da06.docx','previews/a3df98ae9503374fa45e77e6959c53b4ae8f602c315699ec6b9c0b486e50da06.pdf',NULL,NULL,0,205360,'来源：三下（高新前湾）/第6课 《数字作品面面观》教学设计.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb20264064a97a4b50134c7bffe20ea9fd1086'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb202697e877557229f4d62d65a6db057e1924',b.book_id,'lesson-7','处理图像素材','张燕睿','宁波高新区信懋小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-7' AND s.kind='LESSON'
WHERE b.book_code='xxkj-3-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'7059ef620d9ac6dc9f8a1c17e2031b9e2b0d74b9a8579554d851ab32f048e61e','DESIGN','第7课 《处理图像素材》教学设计','第7课 《处理图像素材》教学设计.docx','70/7059ef620d9ac6dc9f8a1c17e2031b9e2b0d74b9a8579554d851ab32f048e61e.docx','previews/7059ef620d9ac6dc9f8a1c17e2031b9e2b0d74b9a8579554d851ab32f048e61e.pdf',NULL,NULL,0,42775,'来源：三下（高新前湾）/第7课 《处理图像素材》教学设计.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb202697e877557229f4d62d65a6db057e1924'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026b83dab255086d3d03196c041c41c5204',b.book_id,'lesson-8','剪辑音频素材','罗洁','高新区信懋小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-8' AND s.kind='LESSON'
WHERE b.book_code='xxkj-3-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'2bb030a54ce664e8496056bb8ad1393ca21db385d3738a16cbf66b88cecd593c','DESIGN','第8课 《剪辑音频素材》 教学设计','第8课 《剪辑音频素材》 教学设计.docx','2b/2bb030a54ce664e8496056bb8ad1393ca21db385d3738a16cbf66b88cecd593c.docx','previews/2bb030a54ce664e8496056bb8ad1393ca21db385d3738a16cbf66b88cecd593c.pdf',NULL,NULL,0,73885,'来源：三下（高新前湾）/第8课 《剪辑音频素材》 教学设计.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026b83dab255086d3d03196c041c41c5204'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026bd77bfb4088ab7a60cfeff8ac5d2e4b2',b.book_id,'lesson-9','编辑视频素材','倪双双','宁波宁波前湾新区滨海小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-9' AND s.kind='LESSON'
WHERE b.book_code='xxkj-3-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'25d81acb5504dec358237642ccc9be679648e462dc3bee7148acc610817476c9','MATERIAL','9《编辑视频素材》 倪双双','9《编辑视频素材》 倪双双.docx','25/25d81acb5504dec358237642ccc9be679648e462dc3bee7148acc610817476c9.docx','previews/25d81acb5504dec358237642ccc9be679648e462dc3bee7148acc610817476c9.pdf',NULL,NULL,0,160471,'来源：三下（高新前湾）/9《编辑视频素材》 倪双双.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026bd77bfb4088ab7a60cfeff8ac5d2e4b2'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026df4b23801ccce815843e990c29498141',b.book_id,'lesson-10','创作发布作品','萧潇','宁波前湾新区世纪城实验小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-10' AND s.kind='LESSON'
WHERE b.book_code='xxkj-3-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'95ea1dec9732403dbbd98a5df4bdd1b17e47d5be64859a1b797409e5e78d16e2','DESIGN','10《创作发布作品》','10《创作发布作品》.docx','95/95ea1dec9732403dbbd98a5df4bdd1b17e47d5be64859a1b797409e5e78d16e2.docx','previews/95ea1dec9732403dbbd98a5df4bdd1b17e47d5be64859a1b797409e5e78d16e2.pdf',NULL,NULL,0,382490,'来源：三下（高新前湾）/10《创作发布作品》.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026df4b23801ccce815843e990c29498141'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-e5bc4b8937b1a399c65b6f89ca11a24bd407af4956a2231e15f4ee5493f102e3','MATERIAL','资源智能生成-前湾新区-第11课《资源智能生成》','第11课-资源智能生成-卢旭浩-前湾新区-第11课《资源智能生成》.zip','e5/e5bc4b8937b1a399c65b6f89ca11a24bd407af4956a2231e15f4ee5493f102e3.zip',NULL,NULL,NULL,0,23115,'共 4 个文件，来源目录：三下（高新前湾）/前湾新区-第11课《资源智能生成》-卢旭浩','解压后用浏览器打开入口 HTML；若页面依赖本地服务或模型文件，请按包内 README 启动。',1
FROM biz_textbook_case c WHERE c.source_key='1d48c49c851f04fe2f1917ebd8dabadd'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-e113bb2638ede812a64e0f9ab42ae8d63c233a98f5c6fe37b3593a070d3a6d15','MATERIAL','资源智能生成-前湾新区-第11课《资源智能生成》','第11课-资源智能生成-岑姝锌-前湾新区-第11课《资源智能生成》.zip','e1/e113bb2638ede812a64e0f9ab42ae8d63c233a98f5c6fe37b3593a070d3a6d15.zip',NULL,NULL,NULL,0,6003785,'共 9 个文件，来源目录：三下（高新前湾）/前湾新区-第11课《资源智能生成》-岑姝锌','解压后用浏览器打开入口 HTML；若页面依赖本地服务或模型文件，请按包内 README 启动。\n含 Windows 程序或脚本，解压后按包内说明运行；请先确认来源可信。',1
FROM biz_textbook_case c WHERE c.source_key='6b0f148ae1b7d79ea2749fa09d1f16c4'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-7fc749ea0f274dacacf954ae004452bf2f38fedd1a826658a70d68adc56a2bc4','MATERIAL','资源智能生成-前湾新区-第11课《资源智能生成》','第11课-资源智能生成-张楚人-前湾新区-第11课《资源智能生成》.zip','7f/7fc749ea0f274dacacf954ae004452bf2f38fedd1a826658a70d68adc56a2bc4.zip',NULL,NULL,NULL,0,32179515,'共 23 个文件，来源目录：三下（高新前湾）/前湾新区-第11课《资源智能生成》-张楚人','解压后用浏览器打开入口 HTML；若页面依赖本地服务或模型文件，请按包内 README 启动。\n含 Windows 程序或脚本，解压后按包内说明运行；请先确认来源可信。',1
FROM biz_textbook_case c WHERE c.source_key='269d796e11a880afb11adc1a39f4e884'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-6f5da31fe65edf12243655811162c949ad8f8fb274d96ec32603b78cc7620689','MATERIAL','资源智能生成-前湾新区-第11课《资源智能生成》','第11课-资源智能生成-王诗莹-前湾新区-第11课《资源智能生成》.zip','6f/6f5da31fe65edf12243655811162c949ad8f8fb274d96ec32603b78cc7620689.zip',NULL,NULL,NULL,0,61243,'共 3 个文件，来源目录：三下（高新前湾）/前湾新区-第11课《资源智能生成》-王诗莹','解压后用浏览器打开入口 HTML；若页面依赖本地服务或模型文件，请按包内 README 启动。',1
FROM biz_textbook_case c WHERE c.source_key='6263dc90ac509dcdff596666c3b972e4'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-c471fc3d572922605410953d51901163a495d71b87e7ae45c3a7a93094d85f73','MATERIAL','资源智能生成-前湾新区-第11课《资源智能生成》','第11课-资源智能生成-陈凯-前湾新区-第11课《资源智能生成》.zip','c4/c471fc3d572922605410953d51901163a495d71b87e7ae45c3a7a93094d85f73.zip',NULL,NULL,NULL,0,2788842,'共 30 个文件，来源目录：三下（高新前湾）/前湾新区-第11课《资源智能生成》-陈凯','需 Node.js 环境：解压后在项目目录执行 npm install，再按 README 或 package.json 启动。\n解压后用浏览器打开入口 HTML；若页面依赖本地服务或模型文件，请按包内 README 启动。\n含 Windows 程序或脚本，解压后按包内说明运行；请先确认来源可信。',1
FROM biz_textbook_case c WHERE c.source_key='a704d012af6b44e31d90eb373db60465'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026a5c9fc145cf32a007f863ae774a867aa',b.book_id,'lesson-1','身边的数据','丁程遥','','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-1' AND s.kind='LESSON'
WHERE b.book_code='xxkj-4-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'ecbc59ab907042627d3014c066a31afacb3b2ee64e670261ba30da82b1e76cf0','DESIGN','第1课《身边的数据》教学设计(丁程遥)','第1课《身边的数据》教学设计(丁程遥).pdf','ec/ecbc59ab907042627d3014c066a31afacb3b2ee64e670261ba30da82b1e76cf0.pdf','ec/ecbc59ab907042627d3014c066a31afacb3b2ee64e670261ba30da82b1e76cf0.pdf',NULL,NULL,0,247477,'来源：四上（北仑）/第1课《身边的数据》教学设计(丁程遥).pdf','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026a5c9fc145cf32a007f863ae774a867aa'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026b54869c70261c75a436321e5d32a7bed',b.book_id,'lesson-1','身边的数据','朱英','宁波东海实验学校','NINGBO2026','PUBLISHED',102
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-1' AND s.kind='LESSON'
WHERE b.book_code='xxkj-4-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'4105840ea31c1ccbf010df40a9b31980a84b341e143c8b538819b38bfdf28a3b','DESIGN','第1课 《身边的数据》教学设计(朱英)','第1课 《身边的数据》教学设计(朱英).docx','41/4105840ea31c1ccbf010df40a9b31980a84b341e143c8b538819b38bfdf28a3b.docx','previews/4105840ea31c1ccbf010df40a9b31980a84b341e143c8b538819b38bfdf28a3b.pdf',NULL,NULL,0,158664,'来源：四上（北仑）/第1课 《身边的数据》教学设计(朱英).docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026b54869c70261c75a436321e5d32a7bed'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb20267e7632393c4796de90131dd3de465ab6',b.book_id,'lesson-2','多样的数据','史敏敏','北仑区滨海新城实验学校','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-2' AND s.kind='LESSON'
WHERE b.book_code='xxkj-4-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'22010d660c009e4d778c1cbf6bd9871abb890c1a6dd697bd4fe67fea96cdeca8','DESIGN','第2课《多样的数据》教学设计(史敏敏)','第2课《多样的数据》教学设计(史敏敏).docx','22/22010d660c009e4d778c1cbf6bd9871abb890c1a6dd697bd4fe67fea96cdeca8.docx','previews/22010d660c009e4d778c1cbf6bd9871abb890c1a6dd697bd4fe67fea96cdeca8.pdf',NULL,NULL,0,1160376,'来源：四上（北仑）/第2课《多样的数据》教学设计(史敏敏).docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb20267e7632393c4796de90131dd3de465ab6'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026b40fb3df0a8587ecfc0c2f695f6b8ca7',b.book_id,'lesson-2','多样的数据','虞珊华','','NINGBO2026','PUBLISHED',102
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-2' AND s.kind='LESSON'
WHERE b.book_code='xxkj-4-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'737f08d40ab17b7ddbe470557250f3c7a37551421d96bc0f3f6fd1f3a3b53997','DESIGN','第2课《多样的数据》教学设计(虞珊华)','第2课《多样的数据》教学设计(虞珊华).docx','73/737f08d40ab17b7ddbe470557250f3c7a37551421d96bc0f3f6fd1f3a3b53997.docx','previews/737f08d40ab17b7ddbe470557250f3c7a37551421d96bc0f3f6fd1f3a3b53997.pdf',NULL,NULL,0,236296,'来源：四上（北仑）/第2课《多样的数据》教学设计(虞珊华).docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026b40fb3df0a8587ecfc0c2f695f6b8ca7'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb202649224264d9fd05f79e2c88714fd55852',b.book_id,'lesson-2','多样的数据','贺霞','','NINGBO2026','PUBLISHED',103
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-2' AND s.kind='LESSON'
WHERE b.book_code='xxkj-4-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'762ead90615cbafa43a76cbd94172a57b5271d41bba8b5090213e95131d3708c','DESIGN','第2课《多样的数据》教学设计(贺霞)','第2课《多样的数据》教学设计(贺霞).docx','76/762ead90615cbafa43a76cbd94172a57b5271d41bba8b5090213e95131d3708c.docx','previews/762ead90615cbafa43a76cbd94172a57b5271d41bba8b5090213e95131d3708c.pdf',NULL,NULL,0,37194,'来源：四上（北仑）/第2课《多样的数据》教学设计(贺霞).docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb202649224264d9fd05f79e2c88714fd55852'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026ffd45945b5f80bddf8690d78511c8b92',b.book_id,'lesson-3','数据的价值','胡佩芬','岷山学校','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-3' AND s.kind='LESSON'
WHERE b.book_code='xxkj-4-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'629aae34aa9e621a1992725a353d9cda847f24eaa9134c896a955af107e514e9','DESIGN','第3课 《数据的价值》教学设计(胡佩芬)','第3课 《数据的价值》教学设计(胡佩芬).docx','62/629aae34aa9e621a1992725a353d9cda847f24eaa9134c896a955af107e514e9.docx','previews/629aae34aa9e621a1992725a353d9cda847f24eaa9134c896a955af107e514e9.pdf',NULL,NULL,0,22415,'来源：四上（北仑）/第3课 《数据的价值》教学设计(胡佩芬).docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026ffd45945b5f80bddf8690d78511c8b92'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026b8487e09dfa80a012c39001ff3fae3f8',b.book_id,'lesson-4','数据的安全','周芮','宁波市北仑区灵山书院','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-4' AND s.kind='LESSON'
WHERE b.book_code='xxkj-4-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'0dbddfb0295832423ff03f4243bb3f501c1371fec5e979d07a219b36461d5c1c','DESIGN','第4课《数据的安全》教学设计(周芮)','第4课《数据的安全》教学设计(周芮).docx','0d/0dbddfb0295832423ff03f4243bb3f501c1371fec5e979d07a219b36461d5c1c.docx','previews/0dbddfb0295832423ff03f4243bb3f501c1371fec5e979d07a219b36461d5c1c.pdf',NULL,NULL,0,72373,'来源：四上（北仑）/第4课《数据的安全》教学设计(周芮).docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026b8487e09dfa80a012c39001ff3fae3f8'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026f8346a35d99b7cbea279d85a571a9293',b.book_id,'lesson-5','数据获取','项琴燕','','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-5' AND s.kind='LESSON'
WHERE b.book_code='xxkj-4-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'c6a668f2f2bb82691e51b3a35c9360cef67adcb0c58402dc1ac4cd59cd627512','DESIGN','第5课 《数据获取》教学设计(项琴燕)','第5课 《数据获取》教学设计(项琴燕).pdf','c6/c6a668f2f2bb82691e51b3a35c9360cef67adcb0c58402dc1ac4cd59cd627512.pdf','c6/c6a668f2f2bb82691e51b3a35c9360cef67adcb0c58402dc1ac4cd59cd627512.pdf',NULL,NULL,0,400852,'来源：四上（北仑）/第5课 《数据获取》教学设计(项琴燕).pdf','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026f8346a35d99b7cbea279d85a571a9293'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb202600391a543f08460353b08146cefb461f',b.book_id,'lesson-6','数据整理','乐思涵','北仑区高塘学校','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-6' AND s.kind='LESSON'
WHERE b.book_code='xxkj-4-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'ee201305e45b8cf40cd4e0deac0c8291ddb2288388e7ae35409451735eacc535','DESIGN','第6课《数据整理》教学设计(乐思涵)','第6课《数据整理》教学设计(乐思涵).docx','ee/ee201305e45b8cf40cd4e0deac0c8291ddb2288388e7ae35409451735eacc535.docx','previews/ee201305e45b8cf40cd4e0deac0c8291ddb2288388e7ae35409451735eacc535.pdf',NULL,NULL,0,43613,'来源：四上（北仑）/第6课《数据整理》教学设计(乐思涵).docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb202600391a543f08460353b08146cefb461f'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026bd3a0bbc2fbce0e58eaa9847f260daf9',b.book_id,'lesson-7','数据计算','袁燕娜','宁波市北仑区新碶小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-7' AND s.kind='LESSON'
WHERE b.book_code='xxkj-4-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'c024b63810b84f06dae140298d54f3a40c4925f2213860da5fa13df2a733cc8c','DESIGN','第7课《数据计算》教学设计(袁燕娜)','第7课《数据计算》教学设计(袁燕娜).docx','c0/c024b63810b84f06dae140298d54f3a40c4925f2213860da5fa13df2a733cc8c.docx','previews/c024b63810b84f06dae140298d54f3a40c4925f2213860da5fa13df2a733cc8c.pdf',NULL,NULL,0,69184,'来源：四上（北仑）/第7课《数据计算》教学设计(袁燕娜).docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026bd3a0bbc2fbce0e58eaa9847f260daf9'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb202650e400bfdf0719666b6d4b9cfeb810b8',b.book_id,'lesson-8','图表呈现','顾嘉琪','北仑区高塘学校','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-8' AND s.kind='LESSON'
WHERE b.book_code='xxkj-4-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'ea4390a1c2eea6ad3de779cdd02392c1c9a0d9feefc0c2d8db53f5c8fba95d49','DESIGN','第8课《图表呈现》教学设计(顾嘉琪)','第8课《图表呈现》教学设计(顾嘉琪).docx','ea/ea4390a1c2eea6ad3de779cdd02392c1c9a0d9feefc0c2d8db53f5c8fba95d49.docx','previews/ea4390a1c2eea6ad3de779cdd02392c1c9a0d9feefc0c2d8db53f5c8fba95d49.pdf',NULL,NULL,0,61125,'来源：四上（北仑）/第8课《图表呈现》教学设计(顾嘉琪).docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb202650e400bfdf0719666b6d4b9cfeb810b8'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb202648b0bbcd27cf8fab5d38c492986fcfcf',b.book_id,'lesson-9','数据解读与分析','王梦梦','北仑区新碶小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-9' AND s.kind='LESSON'
WHERE b.book_code='xxkj-4-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'94c6aef7f5acfeaf414ec9684f23012d0809d76c7bd316e17997a9908836eabe','DESIGN','第9课《数据解读与分析》教学设计(王梦梦)','第9课《数据解读与分析》教学设计(王梦梦).docx','94/94c6aef7f5acfeaf414ec9684f23012d0809d76c7bd316e17997a9908836eabe.docx','previews/94c6aef7f5acfeaf414ec9684f23012d0809d76c7bd316e17997a9908836eabe.pdf',NULL,NULL,0,231373,'来源：四上（北仑）/第9课《数据解读与分析》教学设计(王梦梦).docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb202648b0bbcd27cf8fab5d38c492986fcfcf'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb20265b7e70951dd7d2535b9de82ebad0547d',b.book_id,'lesson-10','从数据到编码','黄志芬','北仑区大碶小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-10' AND s.kind='LESSON'
WHERE b.book_code='xxkj-4-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'eabc4318d16583210b12b4da296be03c15c8b6da4f8f540a574b2119e48ea73a','DESIGN','第10课《从数据到编码》教学设计(黄志芬)','第10课《从数据到编码》教学设计(黄志芬).docx','ea/eabc4318d16583210b12b4da296be03c15c8b6da4f8f540a574b2119e48ea73a.docx','previews/eabc4318d16583210b12b4da296be03c15c8b6da4f8f540a574b2119e48ea73a.pdf',NULL,NULL,0,515812,'来源：四上（北仑）/第10课《从数据到编码》教学设计(黄志芬).docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb20265b7e70951dd7d2535b9de82ebad0547d'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb20262533f3435e7a342ef6c02e1b8e9228cd',b.book_id,'lesson-11','有序的世界','郑仙','白峰小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-11' AND s.kind='LESSON'
WHERE b.book_code='xxkj-4-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'014cb57aef692da6d23fe654832408ab66bc46328204b6f5a250d0a9d32d27b0','DESIGN','第11课《有序的世界》教学设计(郑仙)','第11课《有序的世界》教学设计(郑仙).docx','01/014cb57aef692da6d23fe654832408ab66bc46328204b6f5a250d0a9d32d27b0.docx','previews/014cb57aef692da6d23fe654832408ab66bc46328204b6f5a250d0a9d32d27b0.pdf',NULL,NULL,0,41886,'来源：四上（北仑）/第11课《有序的世界》教学设计(郑仙).docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb20262533f3435e7a342ef6c02e1b8e9228cd'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026583edd736d3f44e77fdada6367bf0e7c',b.book_id,'lesson-12','编码长度与信息量','郑煜','','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-12' AND s.kind='LESSON'
WHERE b.book_code='xxkj-4-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'44d9052bd971c75e319a70ade577300bf883b3e5d2aebe626e68ed94d0b38503','DESIGN','第12课《编码长度与信息量》教学设计(郑煜)','第12课《编码长度与信息量》教学设计(郑煜).docx','44/44d9052bd971c75e319a70ade577300bf883b3e5d2aebe626e68ed94d0b38503.docx','previews/44d9052bd971c75e319a70ade577300bf883b3e5d2aebe626e68ed94d0b38503.pdf',NULL,NULL,0,34928,'来源：四上（北仑）/第12课《编码长度与信息量》教学设计(郑煜).docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026583edd736d3f44e77fdada6367bf0e7c'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb202691531ffc1d82373496234ea4aff27b6d',b.book_id,'lesson-13','数据有关联','胡海斌','','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-13' AND s.kind='LESSON'
WHERE b.book_code='xxkj-4-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'74a76a7437fef6092ca51e06526c0424ebb85009df30b24479155d2825c2ec0c','DESIGN','第13课《数据有关联》教学设计(胡海斌)','第13课《数据有关联》教学设计(胡海斌).docx','74/74a76a7437fef6092ca51e06526c0424ebb85009df30b24479155d2825c2ec0c.docx','previews/74a76a7437fef6092ca51e06526c0424ebb85009df30b24479155d2825c2ec0c.pdf',NULL,NULL,0,26625,'来源：四上（北仑）/第13课《数据有关联》教学设计(胡海斌).docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb202691531ffc1d82373496234ea4aff27b6d'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026077846af097d5d161d3416c3c815c8fc',b.book_id,'lesson-13','数据有关联','薛心怡','','NINGBO2026','PUBLISHED',102
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-13' AND s.kind='LESSON'
WHERE b.book_code='xxkj-4-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'1c30cecf59b3368083ffc5b7268080c6a2c98c9593ea0f94ac22c300e3b46c91','DESIGN','第13课 数据有关联 教学设计_顾国和中学 薛心怡','第13课 数据有关联 教学设计_顾国和中学 薛心怡.docx','1c/1c30cecf59b3368083ffc5b7268080c6a2c98c9593ea0f94ac22c300e3b46c91.docx','previews/1c30cecf59b3368083ffc5b7268080c6a2c98c9593ea0f94ac22c300e3b46c91.pdf',NULL,NULL,0,59732,'来源：四上（北仑）/第13课 数据有关联(薛心怡)/第13课 数据有关联 教学设计_顾国和中学 薛心怡.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026077846af097d5d161d3416c3c815c8fc'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'7a254a70bf0a39247fa48fee8e05826aef78a40defea0f354739f370a9be0661','MATERIAL','借阅次数表','借阅次数表.xlsx','7a/7a254a70bf0a39247fa48fee8e05826aef78a40defea0f354739f370a9be0661.xlsx','previews/7a254a70bf0a39247fa48fee8e05826aef78a40defea0f354739f370a9be0661.pdf',NULL,NULL,0,9649,'来源：四上（北仑）/第13课 数据有关联(薛心怡)/任务用表/借阅次数表.xlsx','',2
FROM biz_textbook_case c WHERE c.source_key='nb2026077846af097d5d161d3416c3c815c8fc'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'47580f0933db1406833e091d2d3f6eba97a7d3c77e61867d80b96eb0d0d959a0','MATERIAL','学生信息表','学生信息表.xlsx','47/47580f0933db1406833e091d2d3f6eba97a7d3c77e61867d80b96eb0d0d959a0.xlsx','previews/47580f0933db1406833e091d2d3f6eba97a7d3c77e61867d80b96eb0d0d959a0.pdf',NULL,NULL,0,10673,'来源：四上（北仑）/第13课 数据有关联(薛心怡)/任务用表/学生信息表.xlsx','',3
FROM biz_textbook_case c WHERE c.source_key='nb2026077846af097d5d161d3416c3c815c8fc'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'4c7e2f7841de013bd631f64717debc0cccd28b45a16732241350a403f39d20e0','MATERIAL','校园多项数据表','校园多项数据表.xlsx','4c/4c7e2f7841de013bd631f64717debc0cccd28b45a16732241350a403f39d20e0.xlsx','previews/4c7e2f7841de013bd631f64717debc0cccd28b45a16732241350a403f39d20e0.pdf',NULL,NULL,0,14642,'来源：四上（北仑）/第13课 数据有关联(薛心怡)/任务用表/校园多项数据表.xlsx','',4
FROM biz_textbook_case c WHERE c.source_key='nb2026077846af097d5d161d3416c3c815c8fc'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'d65462f76e8d1f461af4ccddc3aba942ec21b4a584b5458e2e7700eee9b4a63c','WORKSHEET','学习任务单','学习任务单.docx','d6/d65462f76e8d1f461af4ccddc3aba942ec21b4a584b5458e2e7700eee9b4a63c.docx','previews/d65462f76e8d1f461af4ccddc3aba942ec21b4a584b5458e2e7700eee9b4a63c.pdf',NULL,NULL,0,105702,'来源：四上（北仑）/第13课 数据有关联(薛心怡)/学习任务单.docx','',5
FROM biz_textbook_case c WHERE c.source_key='nb2026077846af097d5d161d3416c3c815c8fc'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'61c52c44abf704ca377734720aa5b0ac3732cbdf2c2a825c521ca1515ab3d1b1','VIDEO','数字火炬手','数字火炬手.mp4','61/61c52c44abf704ca377734720aa5b0ac3732cbdf2c2a825c521ca1515ab3d1b1.mp4','61/61c52c44abf704ca377734720aa5b0ac3732cbdf2c2a825c521ca1515ab3d1b1.mp4',NULL,NULL,0,11207387,'来源：四上（北仑）/第13课 数据有关联(薛心怡)/数字火炬手.mp4','',6
FROM biz_textbook_case c WHERE c.source_key='nb2026077846af097d5d161d3416c3c815c8fc'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'d7e9e584df8935f72161b6ba24eda756ba760ec7ebd08d69f37036fa08989529','SLIDES','第13课 数据有关联 课件_顾国和中学 薛心怡','第13课 数据有关联 课件_顾国和中学 薛心怡.pptx','d7/d7e9e584df8935f72161b6ba24eda756ba760ec7ebd08d69f37036fa08989529.pptx','previews/d7e9e584df8935f72161b6ba24eda756ba760ec7ebd08d69f37036fa08989529.pdf',NULL,NULL,0,14629217,'来源：四上（北仑）/第13课 数据有关联(薛心怡)/第13课 数据有关联 课件_顾国和中学 薛心怡.pptx','',7
FROM biz_textbook_case c WHERE c.source_key='nb2026077846af097d5d161d3416c3c815c8fc'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026ad9021003a87cb03a4dee9e1f15a1f25',b.book_id,'lesson-14','编码的规则制订','章敬洋','北仑区泰河学校','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-14' AND s.kind='LESSON'
WHERE b.book_code='xxkj-4-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'1af5715ee748a167d764b4a442a66a1b8bad3fe9d3f77a288353c387cddfd81d','DESIGN','第14课《编码的规则与制定》教学设计(章敬洋)','第14课《编码的规则与制定》教学设计(章敬洋).docx','1a/1af5715ee748a167d764b4a442a66a1b8bad3fe9d3f77a288353c387cddfd81d.docx','previews/1af5715ee748a167d764b4a442a66a1b8bad3fe9d3f77a288353c387cddfd81d.pdf',NULL,NULL,0,557073,'来源：四上（北仑）/第14课《编码的规则与制定》教学设计(章敬洋).docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026ad9021003a87cb03a4dee9e1f15a1f25'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026795ed52afbe04e7afb4c4160f95ca6e4',b.book_id,'lesson-15','编码的验证优化','谢晶','北仑区华山小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-15' AND s.kind='LESSON'
WHERE b.book_code='xxkj-4-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'fbabaa9b3e491788a71b23419c32e91d6a04835568f866c3085374c2be423c28','DESIGN','第15课《编码的验证优化》教学设计(谢晶)','第15课《编码的验证优化》教学设计(谢晶).docx','fb/fbabaa9b3e491788a71b23419c32e91d6a04835568f866c3085374c2be423c28.docx','previews/fbabaa9b3e491788a71b23419c32e91d6a04835568f866c3085374c2be423c28.pdf',NULL,NULL,0,24502,'来源：四上（北仑）/第15课《编码的验证优化》教学设计(谢晶).docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026795ed52afbe04e7afb4c4160f95ca6e4'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-300f9f62dd42320e40fab65bf43899fbb8cc915ebe9861de88e9c9481410b0cb','MATERIAL','数据驱动识花-第16课《数据驱动识花》','第16课-数据驱动识花-童妮娜-第16课《数据驱动识花》.zip','30/300f9f62dd42320e40fab65bf43899fbb8cc915ebe9861de88e9c9481410b0cb.zip',NULL,NULL,NULL,0,27916820,'共 69 个文件，来源目录：四上（北仑）/象山县-第16课《数据驱动识花》','解压后用浏览器打开入口 HTML；若页面依赖本地服务或模型文件，请按包内 README 启动。',1
FROM biz_textbook_case c WHERE c.source_key='ab55286e55efdae8a7a568a4c4444caa'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-5c9c3eeebb10ca4c4e5847636a139e4f3499b722f1029a257eab942ebf16f73a','MATERIAL','返回成语现场-第17课《返回成语现场》','第17课-返回成语现场-薛心怡-第17课《返回成语现场》.zip','5c/5c9c3eeebb10ca4c4e5847636a139e4f3499b722f1029a257eab942ebf16f73a.zip',NULL,NULL,NULL,0,27724,'共 2 个文件，来源目录：四上（北仑）/北仑区-第17课《返回成语现场》','解压后用浏览器打开入口 HTML；若页面依赖本地服务或模型文件，请按包内 README 启动。',1
FROM biz_textbook_case c WHERE c.source_key='256de2c6fbdb54ea0b4e31b6d64b0dff'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-ce5165909938829d2ee7195cd2b1e300f16c3f383efd3edd6648d63c33cb84ea','MATERIAL','返回成语现场-第17课《返回成语现场》','第17课-返回成语现场-郑榴红-第17课《返回成语现场》.zip','ce/ce5165909938829d2ee7195cd2b1e300f16c3f383efd3edd6648d63c33cb84ea.zip',NULL,NULL,NULL,0,219721908,'共 74 个文件，来源目录：四上（北仑）/北仑区-第17课《返回成语现场》(1)','解压后用浏览器打开入口 HTML；若页面依赖本地服务或模型文件，请按包内 README 启动。',1
FROM biz_textbook_case c WHERE c.source_key='85623be16bae8d834617002aeb55c82b'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-09a7da2d59eaed439d039dc553a5a82db8b593eb8079c807b88065d5cb160800','MATERIAL','点阵数字智能识别-第18课《点阵数字智能识别》','第18课-点阵数字智能识别-章敬洋-第18课《点阵数字智能识别》.zip','09/09a7da2d59eaed439d039dc553a5a82db8b593eb8079c807b88065d5cb160800.zip',NULL,NULL,NULL,0,40616,'共 3 个文件，来源目录：四上（北仑）/北仑区-第18课《点阵数字智能识别》','解压后用浏览器打开入口 HTML；若页面依赖本地服务或模型文件，请按包内 README 启动。',1
FROM biz_textbook_case c WHERE c.source_key='37bf03971b217f5fe422fc0fd5ae8fc2'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-beee7e785356c8d564ce1e6dd185c3928dca2721988e0ba48e36ea9512e7c23a','MATERIAL','点阵数字智能识别-第18课《点阵数字智能识别》','第18课-点阵数字智能识别-金辉-第18课《点阵数字智能识别》.zip','be/beee7e785356c8d564ce1e6dd185c3928dca2721988e0ba48e36ea9512e7c23a.zip',NULL,NULL,NULL,0,24266,'共 3 个文件，来源目录：四上（北仑）/北仑区-第18课《点阵数字智能识别》(1)','解压后用浏览器打开入口 HTML；若页面依赖本地服务或模型文件，请按包内 README 启动。',1
FROM biz_textbook_case c WHERE c.source_key='377755f41df5cce20fd41b01f2696e28'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb20263558ae586ac1ff3db900f4c6d8e87759',b.book_id,'lesson-1','初探数字化','丁艳','','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-1' AND s.kind='LESSON'
WHERE b.book_code='xxkj-4-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'1cd2e49152256e662f957ab3099bcc3cbaa820a693cb8294a767fefd098f996f','DESIGN','《初探数字化 》教学设计','《初探数字化 》教学设计.docx','1c/1cd2e49152256e662f957ab3099bcc3cbaa820a693cb8294a767fefd098f996f.docx','previews/1cd2e49152256e662f957ab3099bcc3cbaa820a693cb8294a767fefd098f996f.pdf',NULL,NULL,0,177586,'来源：四下（镇海）/01初探数字化-丁艳/《初探数字化 》教学设计.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb20263558ae586ac1ff3db900f4c6d8e87759'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'87a6ed363a85d25d297aa643aaa9a51fc63e0855130e2db406d325b30353dd9f','SLIDES','《初探数字化》PPT','《初探数字化》PPT.pptx','87/87a6ed363a85d25d297aa643aaa9a51fc63e0855130e2db406d325b30353dd9f.pptx','previews/87a6ed363a85d25d297aa643aaa9a51fc63e0855130e2db406d325b30353dd9f.pdf',NULL,NULL,0,10545057,'来源：四下（镇海）/01初探数字化-丁艳/《初探数字化》PPT.pptx','',2
FROM biz_textbook_case c WHERE c.source_key='nb20263558ae586ac1ff3db900f4c6d8e87759'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026cfdaaf87030072a75f497366eb528bc7',b.book_id,'lesson-2','图像编码','刘元','','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-2' AND s.kind='LESSON'
WHERE b.book_code='xxkj-4-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'965bb4050b31173cf42e47cc4ca80e4edf13c2656eedf1b557d459d53cdccc8f','DESIGN','【教学设计】小小规律，大大道理——《图像编码》','【教学设计】小小规律，大大道理——《图像编码》.docx','96/965bb4050b31173cf42e47cc4ca80e4edf13c2656eedf1b557d459d53cdccc8f.docx','previews/965bb4050b31173cf42e47cc4ca80e4edf13c2656eedf1b557d459d53cdccc8f.pdf',NULL,NULL,0,212615,'来源：四下（镇海）/02图像编码-刘元/【教学设计】小小规律，大大道理——《图像编码》.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026cfdaaf87030072a75f497366eb528bc7'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'5584b8e1b9690e03aa41f8cb82ad21bd287e92dcb18d957c566f5dd8872cec5c','WORKSHEET','【学习单】','【学习单】.docx','55/5584b8e1b9690e03aa41f8cb82ad21bd287e92dcb18d957c566f5dd8872cec5c.docx','previews/5584b8e1b9690e03aa41f8cb82ad21bd287e92dcb18d957c566f5dd8872cec5c.pdf',NULL,NULL,0,425119,'来源：四下（镇海）/02图像编码-刘元/学习单/【学习单】.docx','',2
FROM biz_textbook_case c WHERE c.source_key='nb2026cfdaaf87030072a75f497366eb528bc7'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'7cfbceddaaae973343e113c756f27b8c45be0e7589375328c7a7ad7f6428847d','SLIDES','【教学课件】小小规律，大大道理','【教学课件】小小规律，大大道理.pptx','7c/7cfbceddaaae973343e113c756f27b8c45be0e7589375328c7a7ad7f6428847d.pptx','previews/7cfbceddaaae973343e113c756f27b8c45be0e7589375328c7a7ad7f6428847d.pdf',NULL,NULL,0,4364426,'来源：四下（镇海）/02图像编码-刘元/教学课件/【教学课件】小小规律，大大道理.pptx','',3
FROM biz_textbook_case c WHERE c.source_key='nb2026cfdaaf87030072a75f497366eb528bc7'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'3e38c052be292abb4759683ecbd41aca35898bf74336dd665a8f90df895ae8ad','MATERIAL','工作簿-图像编码','工作簿-图像编码.xls','3e/3e38c052be292abb4759683ecbd41aca35898bf74336dd665a8f90df895ae8ad.xls','previews/3e38c052be292abb4759683ecbd41aca35898bf74336dd665a8f90df895ae8ad.pdf',NULL,NULL,0,39936,'来源：四下（镇海）/02图像编码-刘元/素材/工作簿-图像编码.xls','',4
FROM biz_textbook_case c WHERE c.source_key='nb2026cfdaaf87030072a75f497366eb528bc7'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'c050344cebf8a99012f5dd7a603e7c792b3a97d22fdb4324cba26541e100370a','MATERIAL','工作簿-图像编码','工作簿-图像编码.xlsx','c0/c050344cebf8a99012f5dd7a603e7c792b3a97d22fdb4324cba26541e100370a.xlsx','previews/c050344cebf8a99012f5dd7a603e7c792b3a97d22fdb4324cba26541e100370a.pdf',NULL,NULL,0,10284,'来源：四下（镇海）/02图像编码-刘元/素材/工作簿-图像编码.xlsx','',5
FROM biz_textbook_case c WHERE c.source_key='nb2026cfdaaf87030072a75f497366eb528bc7'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'8aa08eff78efc74766a35b2b093feaf87dcd3675e62c2f0fb034439ef7e9c6d8','MATERIAL','文档-图像编码','文档-图像编码.docx','8a/8aa08eff78efc74766a35b2b093feaf87dcd3675e62c2f0fb034439ef7e9c6d8.docx','previews/8aa08eff78efc74766a35b2b093feaf87dcd3675e62c2f0fb034439ef7e9c6d8.pdf',NULL,NULL,0,15150,'来源：四下（镇海）/02图像编码-刘元/素材/文档-图像编码.docx','',6
FROM biz_textbook_case c WHERE c.source_key='nb2026cfdaaf87030072a75f497366eb528bc7'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'0a405ee6fba5e556780db2c6db1f7fdae50f251d9d6f9b9d3a69c816edad4163','SLIDES','【资料】板书','【资料】板书.pptx','0a/0a405ee6fba5e556780db2c6db1f7fdae50f251d9d6f9b9d3a69c816edad4163.pptx','previews/0a405ee6fba5e556780db2c6db1f7fdae50f251d9d6f9b9d3a69c816edad4163.pdf',NULL,NULL,0,44293,'来源：四下（镇海）/02图像编码-刘元/资料/【资料】板书.pptx','',7
FROM biz_textbook_case c WHERE c.source_key='nb2026cfdaaf87030072a75f497366eb528bc7'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-def7702cae6752574e2c4b4cf47418d28ed8d4eb10f584100fdcd6aa33a7b05d','MATERIAL','图像编码-02图像编码-刘元','第2课-图像编码-刘元-02图像编码-刘元.zip','de/def7702cae6752574e2c4b4cf47418d28ed8d4eb10f584100fdcd6aa33a7b05d.zip',NULL,NULL,NULL,0,8335995,'共 19 个文件，来源目录：四下（镇海）/02图像编码-刘元','解压后按文件名和包内 README 使用。',8
FROM biz_textbook_case c WHERE c.source_key='nb2026cfdaaf87030072a75f497366eb528bc7'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026bbcf9c655c5b1d3edf8b7cad600a9bb9',b.book_id,'lesson-3','字符编码','王少君','','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-3' AND s.kind='LESSON'
WHERE b.book_code='xxkj-4-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'78b8b734b4e0894653cb793b152dca703e936a478b40cae876af097e9dba52ce','DESIGN','数字世界【字符编码】','数字世界【字符编码】.docx','78/78b8b734b4e0894653cb793b152dca703e936a478b40cae876af097e9dba52ce.docx','previews/78b8b734b4e0894653cb793b152dca703e936a478b40cae876af097e9dba52ce.pdf',NULL,NULL,0,117921,'来源：四下（镇海）/03字符编码-王少君/数字世界【字符编码】.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026bbcf9c655c5b1d3edf8b7cad600a9bb9'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'f8f24aec76c918e647d28b444989bf8ae9116b688f8b4b0a9dabf21b2b467d48','SLIDES','数字世界：字符编码','数字世界：字符编码.pptx','f8/f8f24aec76c918e647d28b444989bf8ae9116b688f8b4b0a9dabf21b2b467d48.pptx','previews/f8f24aec76c918e647d28b444989bf8ae9116b688f8b4b0a9dabf21b2b467d48.pdf',NULL,NULL,0,10618606,'来源：四下（镇海）/03字符编码-王少君/数字世界：字符编码.pptx','',2
FROM biz_textbook_case c WHERE c.source_key='nb2026bbcf9c655c5b1d3edf8b7cad600a9bb9'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb202673787cef047cec5c002e624ce12bd6bf',b.book_id,'lesson-4','声音编码','虞亚','','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-4' AND s.kind='LESSON'
WHERE b.book_code='xxkj-4-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'ff87bc5fd2485a9b36341ef5078dcd80f3d8e8a0d2ed6188d0be589107258746','SLIDES','《声音编码》','《声音编码》.pptx','ff/ff87bc5fd2485a9b36341ef5078dcd80f3d8e8a0d2ed6188d0be589107258746.pptx','previews/ff87bc5fd2485a9b36341ef5078dcd80f3d8e8a0d2ed6188d0be589107258746.pdf',NULL,NULL,0,11215637,'来源：四下（镇海）/04声音编码-虞亚/《声音编码》.pptx','',1
FROM biz_textbook_case c WHERE c.source_key='nb202673787cef047cec5c002e624ce12bd6bf'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'728eed24c488b0250d81e7267f87d5fb76ff8cc436609e15107db2bad214b83f','DESIGN','镇海区中心学校  虞亚 《巧用数字软件 揭秘声音编码》','镇海区中心学校  虞亚 《巧用数字软件 揭秘声音编码》.docx','72/728eed24c488b0250d81e7267f87d5fb76ff8cc436609e15107db2bad214b83f.docx','previews/728eed24c488b0250d81e7267f87d5fb76ff8cc436609e15107db2bad214b83f.pdf',NULL,NULL,0,127700,'来源：四下（镇海）/04声音编码-虞亚/镇海区中心学校  虞亚 《巧用数字软件 揭秘声音编码》.docx','',2
FROM biz_textbook_case c WHERE c.source_key='nb202673787cef047cec5c002e624ce12bd6bf'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb202666cd2f4bb3e886f72ffccc7b0822cf36',b.book_id,'lesson-5','视频编码','周华静','','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-5' AND s.kind='LESSON'
WHERE b.book_code='xxkj-4-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'b06f3606686c7e42bd6646c02ec4bdcbc467fe9990bcda8bc244fea832022188','DESIGN','教学设计','教学设计.pdf','b0/b06f3606686c7e42bd6646c02ec4bdcbc467fe9990bcda8bc244fea832022188.pdf','b0/b06f3606686c7e42bd6646c02ec4bdcbc467fe9990bcda8bc244fea832022188.pdf','教学设计.docx','6d/6d73712fe7a0f06a1fcb5a42cfd2f1a1748009a0ca21f87e13b10c7490be17a0.docx',536167,665477,'来源：四下（镇海）/05视频编码-周华静/教学设计.pdf','',1
FROM biz_textbook_case c WHERE c.source_key='nb202666cd2f4bb3e886f72ffccc7b0822cf36'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'f33d1937300adc612efbec1ba42c256fffac1fdbf2713c7bc289b647c6761f5a','WORKSHEET','学习单','学习单.docx','f3/f33d1937300adc612efbec1ba42c256fffac1fdbf2713c7bc289b647c6761f5a.docx','previews/f33d1937300adc612efbec1ba42c256fffac1fdbf2713c7bc289b647c6761f5a.pdf',NULL,NULL,0,192179,'来源：四下（镇海）/05视频编码-周华静/学习单.docx','',2
FROM biz_textbook_case c WHERE c.source_key='nb202666cd2f4bb3e886f72ffccc7b0822cf36'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'972a73737cc864d852d00a8a03ae07023cb3911d9ff8b16d2ef8afdebf241728','SLIDES','课件','课件.pptx','97/972a73737cc864d852d00a8a03ae07023cb3911d9ff8b16d2ef8afdebf241728.pptx','previews/972a73737cc864d852d00a8a03ae07023cb3911d9ff8b16d2ef8afdebf241728.pdf',NULL,NULL,0,3205556,'来源：四下（镇海）/05视频编码-周华静/课件.pptx','',3
FROM biz_textbook_case c WHERE c.source_key='nb202666cd2f4bb3e886f72ffccc7b0822cf36'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'845ae104478c4a63b8d7f4b6c4d5c609cc4759cd41c8fdafa0758b5af6a4f8fa','VIDEO','“万兴喵影”创作微课视频','“万兴喵影”创作微课视频.mp4','84/845ae104478c4a63b8d7f4b6c4d5c609cc4759cd41c8fdafa0758b5af6a4f8fa.mp4','84/845ae104478c4a63b8d7f4b6c4d5c609cc4759cd41c8fdafa0758b5af6a4f8fa.mp4',NULL,NULL,0,26089787,'来源：四下（镇海）/05视频编码-周华静/软件及图片视频资料/“万兴喵影”创作微课视频.mp4','',4
FROM biz_textbook_case c WHERE c.source_key='nb202666cd2f4bb3e886f72ffccc7b0822cf36'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'5cf2cbe9353b0871dc0bc177ecc44ab5a77162a54370a8069ef3aef7b7969a75','VIDEO','亚运会跳水比赛','亚运会跳水比赛.mp4','5c/5cf2cbe9353b0871dc0bc177ecc44ab5a77162a54370a8069ef3aef7b7969a75.mp4','5c/5cf2cbe9353b0871dc0bc177ecc44ab5a77162a54370a8069ef3aef7b7969a75.mp4',NULL,NULL,0,63193482,'来源：四下（镇海）/05视频编码-周华静/软件及图片视频资料/亚运会跳水比赛.mp4','',5
FROM biz_textbook_case c WHERE c.source_key='nb202666cd2f4bb3e886f72ffccc7b0822cf36'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'e0e141bd22bb9a9b189c4f0c74dd3abecf4daf49e9d7f474d95daff24f7e355e','VIDEO','全红婵的故事','全红婵的故事.mp4','e0/e0e141bd22bb9a9b189c4f0c74dd3abecf4daf49e9d7f474d95daff24f7e355e.mp4','e0/e0e141bd22bb9a9b189c4f0c74dd3abecf4daf49e9d7f474d95daff24f7e355e.mp4',NULL,NULL,0,43771450,'来源：四下（镇海）/05视频编码-周华静/软件及图片视频资料/全红婵的故事.mp4','',6
FROM biz_textbook_case c WHERE c.source_key='nb202666cd2f4bb3e886f72ffccc7b0822cf36'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'8864f659706ee7aea54b531b33f5705b2b17bbe03f6540892b8416c68e69790e','VIDEO','视频数字化','视频数字化.mp4','88/8864f659706ee7aea54b531b33f5705b2b17bbe03f6540892b8416c68e69790e.mp4','88/8864f659706ee7aea54b531b33f5705b2b17bbe03f6540892b8416c68e69790e.mp4',NULL,NULL,0,58089513,'来源：四下（镇海）/05视频编码-周华静/软件及图片视频资料/视频数字化.mp4','',7
FROM biz_textbook_case c WHERE c.source_key='nb202666cd2f4bb3e886f72ffccc7b0822cf36'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'1c8f204d0672227861a951aa07539486b41dabcf046146c932a7a21f9e0f9da7','VIDEO','跳水动画视频DIY1','跳水动画视频DIY1.mp4','1c/1c8f204d0672227861a951aa07539486b41dabcf046146c932a7a21f9e0f9da7.mp4','1c/1c8f204d0672227861a951aa07539486b41dabcf046146c932a7a21f9e0f9da7.mp4',NULL,NULL,0,4689118,'来源：四下（镇海）/05视频编码-周华静/软件及图片视频资料/跳水动画视频DIY1.mp4','',8
FROM biz_textbook_case c WHERE c.source_key='nb202666cd2f4bb3e886f72ffccc7b0822cf36'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'5f1cb1a65d9b566e4af03627bf67aabc12ae4ceca889e17101b7e36b9a8fa0de','VIDEO','跳水动画视频DIY2','跳水动画视频DIY2.mp4','5f/5f1cb1a65d9b566e4af03627bf67aabc12ae4ceca889e17101b7e36b9a8fa0de.mp4','5f/5f1cb1a65d9b566e4af03627bf67aabc12ae4ceca889e17101b7e36b9a8fa0de.mp4',NULL,NULL,0,3218219,'来源：四下（镇海）/05视频编码-周华静/软件及图片视频资料/跳水动画视频DIY2.mp4','',9
FROM biz_textbook_case c WHERE c.source_key='nb202666cd2f4bb3e886f72ffccc7b0822cf36'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-f5a36ddd67b6d4cdd81baec809e00b1fd1bb0e681cf0b99eff067e837a7bd983','MATERIAL','视频编码-05视频编码-周华静','第5课-视频编码-周华静-05视频编码-周华静.zip','f5/f5a36ddd67b6d4cdd81baec809e00b1fd1bb0e681cf0b99eff067e837a7bd983.zip',NULL,NULL,NULL,0,306275359,'共 47 个文件，来源目录：四下（镇海）/05视频编码-周华静','解压后按文件名和包内 README 使用。',10
FROM biz_textbook_case c WHERE c.source_key='nb202666cd2f4bb3e886f72ffccc7b0822cf36'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026ccb38d7eba74d0c3302f85b1d8a7c010',b.book_id,'lesson-6','数据解码','陈强强','','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-6' AND s.kind='LESSON'
WHERE b.book_code='xxkj-4-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'6bb212a3868a12c9ebf768a903b0696a2d56603a38a2904126f7a862ae57b772','DESIGN','教学设计数据的编码 陈强强','教学设计数据的编码 陈强强.docx','6b/6bb212a3868a12c9ebf768a903b0696a2d56603a38a2904126f7a862ae57b772.docx','previews/6bb212a3868a12c9ebf768a903b0696a2d56603a38a2904126f7a862ae57b772.pdf',NULL,NULL,0,89635,'来源：四下（镇海）/06数据的解码-陈强强/教学设计数据的编码 陈强强.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026ccb38d7eba74d0c3302f85b1d8a7c010'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'659c0126f3a583b001d8284bb69cfc32a30d212c3f96e7a44106f8250270c5c8','WORKSHEET','学案打印网格图','学案打印网格图.docx','65/659c0126f3a583b001d8284bb69cfc32a30d212c3f96e7a44106f8250270c5c8.docx','previews/659c0126f3a583b001d8284bb69cfc32a30d212c3f96e7a44106f8250270c5c8.pdf',NULL,NULL,0,13756,'来源：四下（镇海）/06数据的解码-陈强强/学生素材/学案打印网格图.docx','',2
FROM biz_textbook_case c WHERE c.source_key='nb2026ccb38d7eba74d0c3302f85b1d8a7c010'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'f3fccdfd956b46526790ac6a970a5523a8c4572f63934ae17fef6260eb7460d8','VIDEO','电子屏显示原理','电子屏显示原理.mp4','f3/f3fccdfd956b46526790ac6a970a5523a8c4572f63934ae17fef6260eb7460d8.mp4','f3/f3fccdfd956b46526790ac6a970a5523a8c4572f63934ae17fef6260eb7460d8.mp4',NULL,NULL,0,1510654,'来源：四下（镇海）/06数据的解码-陈强强/学生素材/电子屏显示原理.mp4','',3
FROM biz_textbook_case c WHERE c.source_key='nb2026ccb38d7eba74d0c3302f85b1d8a7c010'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'7bfaab283203731f85b23591838b9b1fd23e9a93a10d104ed54ceeff25071879','SLIDES','数据的解码','数据的解码.pptx','7b/7bfaab283203731f85b23591838b9b1fd23e9a93a10d104ed54ceeff25071879.pptx','previews/7bfaab283203731f85b23591838b9b1fd23e9a93a10d104ed54ceeff25071879.pdf',NULL,NULL,0,9261757,'来源：四下（镇海）/06数据的解码-陈强强/数据的解码.pptx','',4
FROM biz_textbook_case c WHERE c.source_key='nb2026ccb38d7eba74d0c3302f85b1d8a7c010'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-8c956564f3728af3db5ea513a822bbdd211086bff5f8f878ffbe22030ccd406d','MATERIAL','数据解码-06数据的解码-陈强强','第6课-数据解码-陈强强-06数据的解码-陈强强.zip','8c/8c956564f3728af3db5ea513a822bbdd211086bff5f8f878ffbe22030ccd406d.zip',NULL,NULL,NULL,0,1548261,'共 3 个文件，来源目录：四下（镇海）/06数据的解码-陈强强','解压后按文件名和包内 README 使用。',5
FROM biz_textbook_case c WHERE c.source_key='nb2026ccb38d7eba74d0c3302f85b1d8a7c010'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb202669f5c51f1a5439ad34b08e090ee2b3c6',b.book_id,'lesson-7','数据校验','吴一赟','','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-7' AND s.kind='LESSON'
WHERE b.book_code='xxkj-4-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'30c4de7e510f467fff528b219b9c2a87e49c42a1fe37c0c9b90e0c99dd25efda','DESIGN','第7课数据校验教学设计','第7课数据校验教学设计.docx','30/30c4de7e510f467fff528b219b9c2a87e49c42a1fe37c0c9b90e0c99dd25efda.docx','previews/30c4de7e510f467fff528b219b9c2a87e49c42a1fe37c0c9b90e0c99dd25efda.pdf',NULL,NULL,0,163964,'来源：四下（镇海）/07数据校验-吴一赟/第7课数据校验教学设计.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb202669f5c51f1a5439ad34b08e090ee2b3c6'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'d39efe8e16fac946532f838cf025ebad098e13511d3e0ba4d6302b2accaa4061','VIDEO','1 校验码：数字检验官','1 校验码：数字检验官.mp4','d3/d39efe8e16fac946532f838cf025ebad098e13511d3e0ba4d6302b2accaa4061.mp4','d3/d39efe8e16fac946532f838cf025ebad098e13511d3e0ba4d6302b2accaa4061.mp4',NULL,NULL,0,35771795,'来源：四下（镇海）/07数据校验-吴一赟/教学视频/1 校验码：数字检验官.mp4','',2
FROM biz_textbook_case c WHERE c.source_key='nb202669f5c51f1a5439ad34b08e090ee2b3c6'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'3ee4224d1e766ac754ce37f0dda7f71c90e5a67448b4debcdf91e41f316cf153','VIDEO','2 md5码简介','2 md5码简介.mp4','3e/3ee4224d1e766ac754ce37f0dda7f71c90e5a67448b4debcdf91e41f316cf153.mp4','3e/3ee4224d1e766ac754ce37f0dda7f71c90e5a67448b4debcdf91e41f316cf153.mp4',NULL,NULL,0,4518587,'来源：四下（镇海）/07数据校验-吴一赟/教学视频/2 md5码简介.mp4','',3
FROM biz_textbook_case c WHERE c.source_key='nb202669f5c51f1a5439ad34b08e090ee2b3c6'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'13b59b41e7e7d08ed72e9b74ac23d8513cb9cc827de172c08d4b7db39f526470','VIDEO','3 wps数据有效性','3 wps数据有效性.mp4','13/13b59b41e7e7d08ed72e9b74ac23d8513cb9cc827de172c08d4b7db39f526470.mp4','13/13b59b41e7e7d08ed72e9b74ac23d8513cb9cc827de172c08d4b7db39f526470.mp4',NULL,NULL,0,1697891,'来源：四下（镇海）/07数据校验-吴一赟/教学视频/3 wps数据有效性.mp4','',4
FROM biz_textbook_case c WHERE c.source_key='nb202669f5c51f1a5439ad34b08e090ee2b3c6'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'06d0c0856f5bdb51659b0f230efd7f42b6fdea90bce163c34f6182184d28c7e9','MATERIAL','学生基本信息','学生基本信息.xlsx','06/06d0c0856f5bdb51659b0f230efd7f42b6fdea90bce163c34f6182184d28c7e9.xlsx','previews/06d0c0856f5bdb51659b0f230efd7f42b6fdea90bce163c34f6182184d28c7e9.pdf',NULL,NULL,0,10396,'来源：四下（镇海）/07数据校验-吴一赟/教学辅助资料/学生基本信息.xlsx','',5
FROM biz_textbook_case c WHERE c.source_key='nb202669f5c51f1a5439ad34b08e090ee2b3c6'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'a39119365b6d27b66a6e33dc3cfaabb6df99049f7a2f6510a938e9c7ca41bd1f','SLIDES','第7课数据校验','第7课数据校验.pptx','a3/a39119365b6d27b66a6e33dc3cfaabb6df99049f7a2f6510a938e9c7ca41bd1f.pptx','previews/a39119365b6d27b66a6e33dc3cfaabb6df99049f7a2f6510a938e9c7ca41bd1f.pdf',NULL,NULL,0,54577280,'来源：四下（镇海）/07数据校验-吴一赟/第7课数据校验.pptx','',6
FROM biz_textbook_case c WHERE c.source_key='nb202669f5c51f1a5439ad34b08e090ee2b3c6'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-574acb833a8c3b18dd26196466909673f2e4d7d858a8a6243cc2767e5cb7a571','MATERIAL','数据校验-07数据校验-吴一赟','第7课-数据校验-吴一赟-07数据校验-吴一赟.zip','57/574acb833a8c3b18dd26196466909673f2e4d7d858a8a6243cc2767e5cb7a571.zip',NULL,NULL,NULL,0,68928,'共 6 个文件，来源目录：四下（镇海）/07数据校验-吴一赟','解压后用浏览器打开入口 HTML；若页面依赖本地服务或模型文件，请按包内 README 启动。',7
FROM biz_textbook_case c WHERE c.source_key='nb202669f5c51f1a5439ad34b08e090ee2b3c6'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb202616fe81bf402b4c4778eb25dcd99ee5df',b.book_id,'lesson-8','数据需要保护','朱微君','','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-8' AND s.kind='LESSON'
WHERE b.book_code='xxkj-4-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'20f3aca4c8c2874d0c623fc92ce7aea18c886bf6a873ee2f334da8c1e0306159','MATERIAL','数据需要保护','数据需要保护.docx','20/20f3aca4c8c2874d0c623fc92ce7aea18c886bf6a873ee2f334da8c1e0306159.docx','previews/20f3aca4c8c2874d0c623fc92ce7aea18c886bf6a873ee2f334da8c1e0306159.pdf',NULL,NULL,0,931017,'来源：四下（镇海）/08数据需要保护-朱微君/数据需要保护.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb202616fe81bf402b4c4778eb25dcd99ee5df'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'f6449d2449feff0b2df357e4248c4937505ec734dde8e367e63afa6c1d754c4a','SLIDES','数据需要保护','数据需要保护.pptx','f6/f6449d2449feff0b2df357e4248c4937505ec734dde8e367e63afa6c1d754c4a.pptx','previews/f6449d2449feff0b2df357e4248c4937505ec734dde8e367e63afa6c1d754c4a.pdf',NULL,NULL,0,4093845,'来源：四下（镇海）/08数据需要保护-朱微君/数据需要保护.pptx','',2
FROM biz_textbook_case c WHERE c.source_key='nb202616fe81bf402b4c4778eb25dcd99ee5df'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026763d1223fcf1fc16845cc2c1e4478e9f',b.book_id,'lesson-9','人机编码有差异','徐波','','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-9' AND s.kind='LESSON'
WHERE b.book_code='xxkj-4-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'6eb7aa47463627e5bef66da30d5d728890f3c868d4662c097743c878ae228abd','DESIGN','《第9课 人机编码有差异》教学设计','《第9课 人机编码有差异》教学设计.docx','6e/6eb7aa47463627e5bef66da30d5d728890f3c868d4662c097743c878ae228abd.docx','previews/6eb7aa47463627e5bef66da30d5d728890f3c868d4662c097743c878ae228abd.pdf',NULL,NULL,0,124705,'来源：四下（镇海）/09人机编码有差异-徐波/《第9课 人机编码有差异》教学设计.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026763d1223fcf1fc16845cc2c1e4478e9f'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'b30ebf1120e4025de63ec975a8d5c370d027d835e00750e344b5b7c013334130','MATERIAL','《第9课 人机编码有差异》教学辅助资料','《第9课 人机编码有差异》教学辅助资料.docx','b3/b30ebf1120e4025de63ec975a8d5c370d027d835e00750e344b5b7c013334130.docx','previews/b30ebf1120e4025de63ec975a8d5c370d027d835e00750e344b5b7c013334130.pdf',NULL,NULL,0,1932968,'来源：四下（镇海）/09人机编码有差异-徐波/《第9课 人机编码有差异》教学辅助资料.docx','',2
FROM biz_textbook_case c WHERE c.source_key='nb2026763d1223fcf1fc16845cc2c1e4478e9f'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'8bea503359d3407a93fcd450bbc08c7c4200676d0b62b5e4659d2af794380e31','SLIDES','《第9课 人机编码有差异》课件','《第9课 人机编码有差异》课件.pptx','8b/8bea503359d3407a93fcd450bbc08c7c4200676d0b62b5e4659d2af794380e31.pptx','previews/8bea503359d3407a93fcd450bbc08c7c4200676d0b62b5e4659d2af794380e31.pdf',NULL,NULL,0,1647603,'来源：四下（镇海）/09人机编码有差异-徐波/《第9课 人机编码有差异》课件.pptx','',3
FROM biz_textbook_case c WHERE c.source_key='nb2026763d1223fcf1fc16845cc2c1e4478e9f'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb202654c6dfedf19c75dec34b9b6ced0d7f91',b.book_id,'lesson-10','记录身边的数据','王素军','','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-10' AND s.kind='LESSON'
WHERE b.book_code='xxkj-4-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'3d89239aa2d3444787222fdab9125c6dffd494442df7b3370e01fbe10eb09fdd','DESIGN','教学设计：第10课 《记录身边的数据》','教学设计：第10课 《记录身边的数据》.docx','3d/3d89239aa2d3444787222fdab9125c6dffd494442df7b3370e01fbe10eb09fdd.docx','previews/3d89239aa2d3444787222fdab9125c6dffd494442df7b3370e01fbe10eb09fdd.pdf',NULL,NULL,0,200660,'来源：四下（镇海）/10记录身边的数据-王素军/教学设计：第10课 《记录身边的数据》.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb202654c6dfedf19c75dec34b9b6ced0d7f91'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'4212d6c3ea4b31314151f068a28cb4edfab8402c49c463f0cbb56ce510a97e72','SLIDES','课件：第10课《记录身边的数据》','课件：第10课《记录身边的数据》.pptx','42/4212d6c3ea4b31314151f068a28cb4edfab8402c49c463f0cbb56ce510a97e72.pptx','previews/4212d6c3ea4b31314151f068a28cb4edfab8402c49c463f0cbb56ce510a97e72.pdf',NULL,NULL,0,103365510,'来源：四下（镇海）/10记录身边的数据-王素军/课件：第10课《记录身边的数据》.pptx','',2
FROM biz_textbook_case c WHERE c.source_key='nb202654c6dfedf19c75dec34b9b6ced0d7f91'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026fec80f212023fbd894347af5e39e358a',b.book_id,'lesson-11','设计统计表','林珠','','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-11' AND s.kind='LESSON'
WHERE b.book_code='xxkj-4-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'36b28548de6a41cb672836a0bc0db4836e3eaf4ad7ede74944a1005af5d7c5da','DESIGN','《设计统计表》教学设计','《设计统计表》教学设计.docx','36/36b28548de6a41cb672836a0bc0db4836e3eaf4ad7ede74944a1005af5d7c5da.docx','previews/36b28548de6a41cb672836a0bc0db4836e3eaf4ad7ede74944a1005af5d7c5da.pdf',NULL,NULL,0,399099,'来源：四下（镇海）/11设计统计表-林珠/《设计统计表》教学设计.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026fec80f212023fbd894347af5e39e358a'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'96dc8e020b964e00588af1ed0715dabb39dbf16cc853c3558338c76f3780a4b9','SLIDES','《设计统计表》课件','《设计统计表》课件.pptx','96/96dc8e020b964e00588af1ed0715dabb39dbf16cc853c3558338c76f3780a4b9.pptx','previews/96dc8e020b964e00588af1ed0715dabb39dbf16cc853c3558338c76f3780a4b9.pdf',NULL,NULL,0,7381641,'来源：四下（镇海）/11设计统计表-林珠/《设计统计表》课件.pptx','',2
FROM biz_textbook_case c WHERE c.source_key='nb2026fec80f212023fbd894347af5e39e358a'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'987449cb3961a37b21db91832751e90a61c5ae3937522d15a023a14b5671f4b5','WORKSHEET','《设计统计表》评价表','《设计统计表》评价表.docx','98/987449cb3961a37b21db91832751e90a61c5ae3937522d15a023a14b5671f4b5.docx','previews/987449cb3961a37b21db91832751e90a61c5ae3937522d15a023a14b5671f4b5.pdf',NULL,NULL,0,12268,'来源：四下（镇海）/11设计统计表-林珠/教学素材/《设计统计表》评价表.docx','',3
FROM biz_textbook_case c WHERE c.source_key='nb2026fec80f212023fbd894347af5e39e358a'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'d8ab30c2c97c6b1ebbd5653cf4bf5891770b01aa092f3fac5f951a5558774352','MATERIAL','历年10月26日收集的气象数据','历年10月26日收集的气象数据.docx','d8/d8ab30c2c97c6b1ebbd5653cf4bf5891770b01aa092f3fac5f951a5558774352.docx','previews/d8ab30c2c97c6b1ebbd5653cf4bf5891770b01aa092f3fac5f951a5558774352.pdf',NULL,NULL,0,10678,'来源：四下（镇海）/11设计统计表-林珠/教学素材/历年10月26日收集的气象数据.docx','',4
FROM biz_textbook_case c WHERE c.source_key='nb2026fec80f212023fbd894347af5e39e358a'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'70f7ecd50b2c632850cfeb3c639299d8577279a9e33972b1b528096a78a508fb','MATERIAL','历年10月26日气象数据统计表','历年10月26日气象数据统计表.xlsx','70/70f7ecd50b2c632850cfeb3c639299d8577279a9e33972b1b528096a78a508fb.xlsx','previews/70f7ecd50b2c632850cfeb3c639299d8577279a9e33972b1b528096a78a508fb.pdf',NULL,NULL,0,10269,'来源：四下（镇海）/11设计统计表-林珠/教学素材/历年10月26日气象数据统计表.xlsx','',5
FROM biz_textbook_case c WHERE c.source_key='nb2026fec80f212023fbd894347af5e39e358a'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'db76e2f4f3cc78acc14d44dfcb683f9a3172f44b776eb04fbe4d5ec05a311cfd','SLIDES','探究活动一','探究活动一.pptm','db/db76e2f4f3cc78acc14d44dfcb683f9a3172f44b776eb04fbe4d5ec05a311cfd.pptm','previews/db76e2f4f3cc78acc14d44dfcb683f9a3172f44b776eb04fbe4d5ec05a311cfd.pdf',NULL,NULL,0,92766,'来源：四下（镇海）/11设计统计表-林珠/教学素材/探究活动一.pptm','',6
FROM biz_textbook_case c WHERE c.source_key='nb2026fec80f212023fbd894347af5e39e358a'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026e3a4cbefbaf05423f0097ce78fd09aa8',b.book_id,'lesson-12','数据可视化','章晓伟','','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-12' AND s.kind='LESSON'
WHERE b.book_code='xxkj-4-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'874e46ed03e524f863a38db603579e904a3a5f2e1427bd17336033a1864b50f2','MATERIAL','《数据可视化》实验小学章晓伟','《数据可视化》实验小学章晓伟.docx','87/874e46ed03e524f863a38db603579e904a3a5f2e1427bd17336033a1864b50f2.docx','previews/874e46ed03e524f863a38db603579e904a3a5f2e1427bd17336033a1864b50f2.pdf',NULL,NULL,0,185110,'来源：四下（镇海）/12数据可视化-章晓伟/《数据可视化》实验小学章晓伟.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026e3a4cbefbaf05423f0097ce78fd09aa8'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'70f7ecd50b2c632850cfeb3c639299d8577279a9e33972b1b528096a78a508fb','MATERIAL','历年10月26日气象数据统计表','历年10月26日气象数据统计表.xlsx','70/70f7ecd50b2c632850cfeb3c639299d8577279a9e33972b1b528096a78a508fb.xlsx','previews/70f7ecd50b2c632850cfeb3c639299d8577279a9e33972b1b528096a78a508fb.pdf',NULL,NULL,0,10269,'来源：四下（镇海）/12数据可视化-章晓伟/历年10月26日气象数据统计表.xlsx','',2
FROM biz_textbook_case c WHERE c.source_key='nb2026e3a4cbefbaf05423f0097ce78fd09aa8'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'c5051875403c5a70936c1fa30170b0196a52ccad3a6a3aa823864b4a47c7a21d','SLIDES','镇海区实验小学章晓伟《数据可视化》','镇海区实验小学章晓伟《数据可视化》.pptx','c5/c5051875403c5a70936c1fa30170b0196a52ccad3a6a3aa823864b4a47c7a21d.pptx','previews/c5051875403c5a70936c1fa30170b0196a52ccad3a6a3aa823864b4a47c7a21d.pdf',NULL,NULL,0,56804038,'来源：四下（镇海）/12数据可视化-章晓伟/镇海区实验小学章晓伟《数据可视化》.pptx','',3
FROM biz_textbook_case c WHERE c.source_key='nb2026e3a4cbefbaf05423f0097ce78fd09aa8'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-a85901d20d4660e74c72983b315fa23c24a3331006ee0fe6ae8bbce5d18372b9','MATERIAL','数据可视化-12数据可视化-章晓伟','第12课-数据可视化-章晓伟-12数据可视化-章晓伟.zip','a8/a85901d20d4660e74c72983b315fa23c24a3331006ee0fe6ae8bbce5d18372b9.zip',NULL,NULL,NULL,0,441694,'共 1 个文件，来源目录：四下（镇海）/12数据可视化-章晓伟','解压后按文件名和包内 README 使用。',4
FROM biz_textbook_case c WHERE c.source_key='nb2026e3a4cbefbaf05423f0097ce78fd09aa8'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb202685e22dcd3647bf15259c36576dcf8bfb',b.book_id,'lesson-13','规律与预测','杨滨','','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-13' AND s.kind='LESSON'
WHERE b.book_code='xxkj-4-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'60d91fb0e5949f253313c83c50b689eab779d5d0d300da19a60a83f1f47f63c4','SLIDES','《规律与预测》','《规律与预测》.pptx','60/60d91fb0e5949f253313c83c50b689eab779d5d0d300da19a60a83f1f47f63c4.pptx','previews/60d91fb0e5949f253313c83c50b689eab779d5d0d300da19a60a83f1f47f63c4.pdf',NULL,NULL,0,1680520,'来源：四下（镇海）/13规律与预测-杨滨/《规律与预测》.pptx','',1
FROM biz_textbook_case c WHERE c.source_key='nb202685e22dcd3647bf15259c36576dcf8bfb'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'8bd59d5e6a1b9d0384820577ea876cd14101495a469bca5cb196e0af5d0dd7e2','DESIGN','《规律与预测》杨滨','《规律与预测》杨滨.docx','8b/8bd59d5e6a1b9d0384820577ea876cd14101495a469bca5cb196e0af5d0dd7e2.docx','previews/8bd59d5e6a1b9d0384820577ea876cd14101495a469bca5cb196e0af5d0dd7e2.pdf',NULL,NULL,0,114760,'来源：四下（镇海）/13规律与预测-杨滨/《规律与预测》杨滨.docx','',2
FROM biz_textbook_case c WHERE c.source_key='nb202685e22dcd3647bf15259c36576dcf8bfb'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb20265b3dd59d328e9612dade77694b8c5180',b.book_id,'lesson-14','数据分析报告','陈佩君','','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-14' AND s.kind='LESSON'
WHERE b.book_code='xxkj-4-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'bf4cfb7322c517c3a9cbf1f35b53b00beee9446416b31f888aa918757088fae0','DESIGN','数据分析 报告出彩（教学设计）','数据分析 报告出彩（教学设计）.docx','bf/bf4cfb7322c517c3a9cbf1f35b53b00beee9446416b31f888aa918757088fae0.docx','previews/bf4cfb7322c517c3a9cbf1f35b53b00beee9446416b31f888aa918757088fae0.pdf',NULL,NULL,0,166372,'来源：四下（镇海）/14数据分析-陈佩君/数据分析 报告出彩（教学设计）.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb20265b3dd59d328e9612dade77694b8c5180'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'b2ddda882ad5bc98e44e828a0295f697993d8fbd9b5b5998bd171e03b11f7d26','SLIDES','课件','课件.pptx','b2/b2ddda882ad5bc98e44e828a0295f697993d8fbd9b5b5998bd171e03b11f7d26.pptx','previews/b2ddda882ad5bc98e44e828a0295f697993d8fbd9b5b5998bd171e03b11f7d26.pdf',NULL,NULL,0,1024471,'来源：四下（镇海）/14数据分析-陈佩君/课件.pptx','',2
FROM biz_textbook_case c WHERE c.source_key='nb20265b3dd59d328e9612dade77694b8c5180'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026d0c67c5d49b5032954236d558b8c1475',b.book_id,'lesson-15','分享数据故事','蔡豪杰','','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-15' AND s.kind='LESSON'
WHERE b.book_code='xxkj-4-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'ac943ab98a677a71e5e07d350af5cc61ce16e0604779ec3e813ac9c5488cab0c','SLIDES','分享数据故事（教师课件）','分享数据故事（教师课件）.pptx','ac/ac943ab98a677a71e5e07d350af5cc61ce16e0604779ec3e813ac9c5488cab0c.pptx','previews/ac943ab98a677a71e5e07d350af5cc61ce16e0604779ec3e813ac9c5488cab0c.pdf',NULL,NULL,0,50294738,'来源：四下（镇海）/15分享数据故事-蔡豪杰/分享数据故事（教师课件）.pptx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026d0c67c5d49b5032954236d558b8c1475'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'8e78ff02792da7b33aaf48ca632ff7ade2cb3cfa73dfe459358b9ab6d4d19356','SLIDES','气象预测数据分析报告（模拟学生作品）','气象预测数据分析报告（模拟学生作品）.pptx','8e/8e78ff02792da7b33aaf48ca632ff7ade2cb3cfa73dfe459358b9ab6d4d19356.pptx','previews/8e78ff02792da7b33aaf48ca632ff7ade2cb3cfa73dfe459358b9ab6d4d19356.pdf',NULL,NULL,0,2668615,'来源：四下（镇海）/15分享数据故事-蔡豪杰/气象预测数据分析报告（模拟学生作品）.pptx','',2
FROM biz_textbook_case c WHERE c.source_key='nb2026d0c67c5d49b5032954236d558b8c1475'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'3cca8da4a69fb95d2ec474a30c4af28f13a2c9db405e4e7d55086ac5e5cd0715','MATERIAL','第15课 分享数据故事','第15课 分享数据故事.docx','3c/3cca8da4a69fb95d2ec474a30c4af28f13a2c9db405e4e7d55086ac5e5cd0715.docx','previews/3cca8da4a69fb95d2ec474a30c4af28f13a2c9db405e4e7d55086ac5e5cd0715.pdf',NULL,NULL,0,157661,'来源：四下（镇海）/15分享数据故事-蔡豪杰/第15课 分享数据故事.docx','',3
FROM biz_textbook_case c WHERE c.source_key='nb2026d0c67c5d49b5032954236d558b8c1475'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-95a90a1e0870017f0a9a4e31f6cec8f38e032cdaaab44ef2a8bbf9c7a5346dc4','MATERIAL','手写数字的收集与标注-慈溪市-第16课《手写数字的收集与标注》','第16课-手写数字的收集与标注-曾瑞恒-慈溪市-第16课《手写数字的收集与标注》.zip','95/95a90a1e0870017f0a9a4e31f6cec8f38e032cdaaab44ef2a8bbf9c7a5346dc4.zip',NULL,NULL,NULL,0,189487,'共 1 个文件，来源目录：四下（镇海）/慈溪市-第16课《手写数字的收集与标注》','解压后用浏览器打开入口 HTML；若页面依赖本地服务或模型文件，请按包内 README 启动。',1
FROM biz_textbook_case c WHERE c.source_key='9b8f849c7ee9b0edb649be269e74bb64'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-4a511a68daef1fce04066be0972cfdf24ecfccf43dc97eb11c081a032c865782','MATERIAL','手写数字的识别与应用-慈溪市-第17课《手写数字的识别与应用》','第17课-手写数字的识别与应用-黄钡钡-慈溪市-第17课《手写数字的识别与应用》.zip','4a/4a511a68daef1fce04066be0972cfdf24ecfccf43dc97eb11c081a032c865782.zip',NULL,NULL,NULL,0,187500662,'共 1 个文件，来源目录：四下（镇海）/慈溪市-第17课《手写数字的识别与应用》','解压后按文件名和包内 README 使用。',1
FROM biz_textbook_case c WHERE c.source_key='aad8a160f89eadba3a68d584a0284ecf'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-31511a6832313697fc2e57d66aa0569e2a99a9a23a7a4755d513eb30b10a9111','MATERIAL','人工智能中的数据问题-慈溪市-第18课《人工智能中的数据问题》','第18课-人工智能中的数据问题-徐静莹-慈溪市-第18课《人工智能中的数据问题》.zip','31/31511a6832313697fc2e57d66aa0569e2a99a9a23a7a4755d513eb30b10a9111.zip',NULL,NULL,NULL,0,63767912,'共 71 个文件，来源目录：四下（镇海）/慈溪市-第18课《人工智能中的数据问题》','解压后用浏览器打开入口 HTML；若页面依赖本地服务或模型文件，请按包内 README 启动。',1
FROM biz_textbook_case c WHERE c.source_key='1b7a12780e69bb4fd9915be42bb25291'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026e10ab75e16feb6e500be389d18d934aa',b.book_id,'lesson-1','身边的算法','杨慧','翰香小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-1' AND s.kind='LESSON'
WHERE b.book_code='xxkj-5-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'dd0b0548d31984d88a90661ff63756868450824156d38f68faf94ae1f92e258c','DESIGN','1 《身边的算法》修改版（翰香小学杨慧）','1 《身边的算法》修改版（翰香小学杨慧）.docx','dd/dd0b0548d31984d88a90661ff63756868450824156d38f68faf94ae1f92e258c.docx','previews/dd0b0548d31984d88a90661ff63756868450824156d38f68faf94ae1f92e258c.pdf',NULL,NULL,0,41036,'来源：五上（海曙）/1 《身边的算法》修改版（翰香小学杨慧）.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026e10ab75e16feb6e500be389d18d934aa'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026e71d97fd7ea60dc74f728f7208e29080',b.book_id,'lesson-2','自然语言描述算法','黄栋','宁波市海曙区海曙中心小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-2' AND s.kind='LESSON'
WHERE b.book_code='xxkj-5-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'db4598c1521ded85d8fc44c59271bd1218751a8e34911cc447a66dad35b92d93','DESIGN','2 《自然语言描述算法》黄栋','2 《自然语言描述算法》黄栋.docx','db/db4598c1521ded85d8fc44c59271bd1218751a8e34911cc447a66dad35b92d93.docx','previews/db4598c1521ded85d8fc44c59271bd1218751a8e34911cc447a66dad35b92d93.pdf',NULL,NULL,0,76133,'来源：五上（海曙）/2 《自然语言描述算法》黄栋.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026e71d97fd7ea60dc74f728f7208e29080'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026f601a0e891d8738adf5535e3540be92f',b.book_id,'lesson-3','流程图描述算法','骆怡','宁波市海曙区广济中心小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-3' AND s.kind='LESSON'
WHERE b.book_code='xxkj-5-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'4fdd1148c0340075b4279e02a86280bd67b412869cbccd13d808bd64acc95963','DESIGN','3《流程图描述算法》教学设计','3《流程图描述算法》教学设计.docx','4f/4fdd1148c0340075b4279e02a86280bd67b412869cbccd13d808bd64acc95963.docx','previews/4fdd1148c0340075b4279e02a86280bd67b412869cbccd13d808bd64acc95963.pdf',NULL,NULL,0,65759,'来源：五上（海曙）/3《流程图描述算法》教学设计.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026f601a0e891d8738adf5535e3540be92f'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'d84df0b8f6efd61c3d9a6cc94e4ad57af734879d020f721a49ad430d5bb401a8','DESIGN','3《流程图描述算法》教学设计海曙广济骆怡(1)','3《流程图描述算法》教学设计海曙广济骆怡(1).docx','d8/d84df0b8f6efd61c3d9a6cc94e4ad57af734879d020f721a49ad430d5bb401a8.docx','previews/d84df0b8f6efd61c3d9a6cc94e4ad57af734879d020f721a49ad430d5bb401a8.pdf',NULL,NULL,0,58906,'来源：五上（海曙）/3《流程图描述算法》教学设计海曙广济骆怡(1).docx','',2
FROM biz_textbook_case c WHERE c.source_key='nb2026f601a0e891d8738adf5535e3540be92f'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026ae1ba90befd9d9a0b77117bdd4414948',b.book_id,'lesson-4','算法中的数据','袁周','宁波市海曙外国语学校','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-4' AND s.kind='LESSON'
WHERE b.book_code='xxkj-5-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'743ec3dc81b727f49e1628d9c683b5c53c5ccdf24e5843e00091e165f53d4470','MATERIAL','4《算法中的数据》(1)','4《算法中的数据》(1).docx','74/743ec3dc81b727f49e1628d9c683b5c53c5ccdf24e5843e00091e165f53d4470.docx','previews/743ec3dc81b727f49e1628d9c683b5c53c5ccdf24e5843e00091e165f53d4470.pdf',NULL,NULL,0,39445,'来源：五上（海曙）/4《算法中的数据》(1).docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026ae1ba90befd9d9a0b77117bdd4414948'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026b1e988bfbdf6e1c9a8a4f419a2b150f6',b.book_id,'lesson-5','算法的特征','提交','宁波市海曙外国语学校','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-5' AND s.kind='LESSON'
WHERE b.book_code='xxkj-5-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'c463d03d93ebe8debb670f4c1e5490ad46fc1716811d12108af5ab4675558110','DESIGN','5《算法的特征》（提交）9.28','5《算法的特征》（提交）9.28.docx','c4/c463d03d93ebe8debb670f4c1e5490ad46fc1716811d12108af5ab4675558110.docx','previews/c463d03d93ebe8debb670f4c1e5490ad46fc1716811d12108af5ab4675558110.pdf',NULL,NULL,0,127579,'来源：五上（海曙）/5《算法的特征》（提交）9.28.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026b1e988bfbdf6e1c9a8a4f419a2b150f6'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb20263663eda4fa1c7c3034772a8cd8cb8985',b.book_id,'lesson-6','顺序结构','张文清','宁波市海曙区信谊小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-6' AND s.kind='LESSON'
WHERE b.book_code='xxkj-5-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'36dfc7f9fa4fb21e7ad5c6afcb1be7d30d67cc6bceb5a08d2a4ba18bf521d48f','DESIGN','6 张文清《顺序结构》教案设计（改）','6 张文清《顺序结构》教案设计（改）.docx','36/36dfc7f9fa4fb21e7ad5c6afcb1be7d30d67cc6bceb5a08d2a4ba18bf521d48f.docx','previews/36dfc7f9fa4fb21e7ad5c6afcb1be7d30d67cc6bceb5a08d2a4ba18bf521d48f.pdf',NULL,NULL,0,52005,'来源：五上（海曙）/6 张文清《顺序结构》教案设计（改）.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb20263663eda4fa1c7c3034772a8cd8cb8985'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026cb65906f624c96e5301fd8106e05635a',b.book_id,'lesson-7','分支结构','娄宇翔','宁波市海曙区海曙中心小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-7' AND s.kind='LESSON'
WHERE b.book_code='xxkj-5-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'8a49261cf66c069cb69314777948e8a9f896bb010d8eae4e7e37dfc05aa21ced','DESIGN','7 《分支结构》教学设计','7 《分支结构》教学设计.docx','8a/8a49261cf66c069cb69314777948e8a9f896bb010d8eae4e7e37dfc05aa21ced.docx','previews/8a49261cf66c069cb69314777948e8a9f896bb010d8eae4e7e37dfc05aa21ced.pdf',NULL,NULL,0,134114,'来源：五上（海曙）/7 《分支结构》教学设计.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026cb65906f624c96e5301fd8106e05635a'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb202627bf06a90374c91d857fb61b67b931ea',b.book_id,'lesson-8','双分支结构','曹丹璐','宁波市海曙区雅戈尔实验学校','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-8' AND s.kind='LESSON'
WHERE b.book_code='xxkj-5-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'c97780e7530686e55a923c748a9846b45c22e88293360c3bde64f7f0efd9bcd2','DESIGN','8《双分支结构》教学设计','8《双分支结构》教学设计.docx','c9/c97780e7530686e55a923c748a9846b45c22e88293360c3bde64f7f0efd9bcd2.docx','previews/c97780e7530686e55a923c748a9846b45c22e88293360c3bde64f7f0efd9bcd2.pdf',NULL,NULL,0,260559,'来源：五上（海曙）/8《双分支结构》教学设计.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb202627bf06a90374c91d857fb61b67b931ea'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb20263bd62f0f6e79026ec0e3e5202777d1d4',b.book_id,'lesson-9','体验算法控制','范东明','宁波市海曙区集士港镇中心小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-9' AND s.kind='LESSON'
WHERE b.book_code='xxkj-5-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'f850bc2b7a8effe3b9e33d1e47018c9c66bf3cd29fe5a50501e2640973266085','DESIGN','9《体验算法控制》教学设计 范东明','9《体验算法控制》教学设计 范东明.docx','f8/f850bc2b7a8effe3b9e33d1e47018c9c66bf3cd29fe5a50501e2640973266085.docx','previews/f850bc2b7a8effe3b9e33d1e47018c9c66bf3cd29fe5a50501e2640973266085.pdf',NULL,NULL,0,212709,'来源：五上（海曙）/9《体验算法控制》教学设计 范东明.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb20263bd62f0f6e79026ec0e3e5202777d1d4'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'f008dccfa7e1977dd8645a81c09ed1a90c91a2d78286670042589b0e315c0d51','MATERIAL','全校学生报名数据_1500条','全校学生报名数据_1500条.xlsx','f0/f008dccfa7e1977dd8645a81c09ed1a90c91a2d78286670042589b0e315c0d51.xlsx','previews/f008dccfa7e1977dd8645a81c09ed1a90c91a2d78286670042589b0e315c0d51.pdf',NULL,NULL,0,123757,'来源：五上（海曙）/第11课《认识决策树》/金阳小学-第11课《认识决策树》-杨晶茹/全校学生报名数据_1500条.xlsx','',1
FROM biz_textbook_case c WHERE c.source_key='47cd0564b742f3cd48d829f6f0ad66c5'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'2f970c28e0fd1349ccc75e6dfa26228ebbe3495b176c676460d7f16aa27ea136','MATERIAL','全校学生数据_1000条模拟数据','全校学生数据_1000条模拟数据.xlsx','2f/2f970c28e0fd1349ccc75e6dfa26228ebbe3495b176c676460d7f16aa27ea136.xlsx','previews/2f970c28e0fd1349ccc75e6dfa26228ebbe3495b176c676460d7f16aa27ea136.pdf',NULL,NULL,0,39524,'来源：五上（海曙）/第11课《认识决策树》/金阳小学-第11课《认识决策树》-杨晶茹/全校学生数据_1000条模拟数据.xlsx','',2
FROM biz_textbook_case c WHERE c.source_key='47cd0564b742f3cd48d829f6f0ad66c5'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-f746d5832256a6210aff2ae7d9320e2ef6cabda1b7955571a1c7ad060f2eec4b','MATERIAL','认识决策树-第11课《认识决策树》','第11课-认识决策树-杨晶茹-第11课《认识决策树》.zip','f7/f746d5832256a6210aff2ae7d9320e2ef6cabda1b7955571a1c7ad060f2eec4b.zip',NULL,NULL,NULL,0,30866,'共 3 个文件，来源目录：五上（海曙）/金阳小学-第11课《认识决策树》-杨晶茹','解压后用浏览器打开入口 HTML；若页面依赖本地服务或模型文件，请按包内 README 启动。',3
FROM biz_textbook_case c WHERE c.source_key='47cd0564b742f3cd48d829f6f0ad66c5'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026c2b182aa8b837d6d3ff3597a07cf011f',b.book_id,'lesson-11','认识决策树','田甜','','NINGBO2026','PUBLISHED',102
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-11' AND s.kind='LESSON'
WHERE b.book_code='xxkj-5-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'c05690ace80aa1f6b1cc7bde54ee7a498a411ddaf990522204d878c63cf9521d','MATERIAL','依托数据建模 解锁决策智慧（10.9新）','依托数据建模 解锁决策智慧（10.9新）.docx','c0/c05690ace80aa1f6b1cc7bde54ee7a498a411ddaf990522204d878c63cf9521d.docx','previews/c05690ace80aa1f6b1cc7bde54ee7a498a411ddaf990522204d878c63cf9521d.pdf',NULL,NULL,0,1608284,'来源：五上（海曙）/第11课《认识决策树》/岔路小学-第11课《认识决策树》-田甜/依托数据建模 解锁决策智慧（10.9新）.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026c2b182aa8b837d6d3ff3597a07cf011f'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'5bb60db6046c707e8b8e2d173cbd1ca6c305e19b17574a9ef5165eda523cab03','SLIDES','认识决策树·课堂PPT','认识决策树·课堂PPT.pptx','5b/5bb60db6046c707e8b8e2d173cbd1ca6c305e19b17574a9ef5165eda523cab03.pptx','previews/5bb60db6046c707e8b8e2d173cbd1ca6c305e19b17574a9ef5165eda523cab03.pdf',NULL,NULL,0,1551602,'来源：五上（海曙）/第11课《认识决策树》/岔路小学-第11课《认识决策树》-田甜/认识决策树·课堂PPT.pptx','',2
FROM biz_textbook_case c WHERE c.source_key='nb2026c2b182aa8b837d6d3ff3597a07cf011f'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-0d930c0aa73a48b08cf269dc5a9269259c430c747f8fb303ec5ab23c797fe854','MATERIAL','认识决策树-第11课《认识决策树》','第11课-认识决策树-田甜-第11课《认识决策树》.zip','0d/0d930c0aa73a48b08cf269dc5a9269259c430c747f8fb303ec5ab23c797fe854.zip',NULL,NULL,NULL,0,1298243,'共 11 个文件，来源目录：五上（海曙）/岔路小学-第11课《认识决策树》-田甜','解压后用浏览器打开入口 HTML；若页面依赖本地服务或模型文件，请按包内 README 启动。',3
FROM biz_textbook_case c WHERE c.source_key='nb2026c2b182aa8b837d6d3ff3597a07cf011f'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb202661fd10b66502dbcb74271ee6861421b4',b.book_id,'lesson-12','问题的分解','傅新宇','','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-12' AND s.kind='LESSON'
WHERE b.book_code='xxkj-5-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'0208720a98a18fa4a8a483bffa738f89140035fd0ed97e85d95d3114c9862535','DESIGN','12【教学设计】第12课 《问题的分解》-宁波市实验学校-傅新宇','12【教学设计】第12课 《问题的分解》-宁波市实验学校-傅新宇.pdf','02/0208720a98a18fa4a8a483bffa738f89140035fd0ed97e85d95d3114c9862535.pdf','02/0208720a98a18fa4a8a483bffa738f89140035fd0ed97e85d95d3114c9862535.pdf',NULL,NULL,0,392904,'来源：五上（海曙）/12【教学设计】第12课 《问题的分解》-宁波市实验学校-傅新宇.pdf','',1
FROM biz_textbook_case c WHERE c.source_key='nb202661fd10b66502dbcb74271ee6861421b4'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb202655f3f352ab8400707b9460e5d8cf2d30',b.book_id,'lesson-13','问题的抽象','王碧芳','海曙区储能学校丽园校区','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-13' AND s.kind='LESSON'
WHERE b.book_code='xxkj-5-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'10405b19ccf9297bc594801c135eeb7efc3df0f71a9773df6cdda2f7557fc3e4','DESIGN','13《 问题的抽象》教学设计 （海曙区储能学校丽园校区 王碧芳）0','13《 问题的抽象》教学设计 （海曙区储能学校丽园校区 王碧芳）0.docx','10/10405b19ccf9297bc594801c135eeb7efc3df0f71a9773df6cdda2f7557fc3e4.docx','previews/10405b19ccf9297bc594801c135eeb7efc3df0f71a9773df6cdda2f7557fc3e4.pdf',NULL,NULL,0,58533,'来源：五上（海曙）/13《 问题的抽象》教学设计 （海曙区储能学校丽园校区 王碧芳）0.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb202655f3f352ab8400707b9460e5d8cf2d30'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026d03f070f1e8102dbb7163b31997b5567',b.book_id,'lesson-14','模型的建立','杜武杰','宁波市孙文英小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-14' AND s.kind='LESSON'
WHERE b.book_code='xxkj-5-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'beeb7d0aa1b92eceb6de21a41517fa67e2a9ba57db21d42e7b418a5ba8586b25','DESIGN','14《模型的建立》 杜武杰','14《模型的建立》 杜武杰.docx','be/beeb7d0aa1b92eceb6de21a41517fa67e2a9ba57db21d42e7b418a5ba8586b25.docx','previews/beeb7d0aa1b92eceb6de21a41517fa67e2a9ba57db21d42e7b418a5ba8586b25.pdf',NULL,NULL,0,72858,'来源：五上（海曙）/14《模型的建立》 杜武杰.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026d03f070f1e8102dbb7163b31997b5567'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026c25b1cc68947286d80b8cd44bf57dbb6',b.book_id,'lesson-15','算法的设计','陈虹','古林镇实验小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-15' AND s.kind='LESSON'
WHERE b.book_code='xxkj-5-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'f3ee4e533358c65910b5d638c65cfa432376b8126c8a919d3acf56ce8c072f51','DESIGN','15《算法的设计》教学设计_陈虹','15《算法的设计》教学设计_陈虹.docx','f3/f3ee4e533358c65910b5d638c65cfa432376b8126c8a919d3acf56ce8c072f51.docx','previews/f3ee4e533358c65910b5d638c65cfa432376b8126c8a919d3acf56ce8c072f51.pdf',NULL,NULL,0,281209,'来源：五上（海曙）/15《算法的设计》教学设计_陈虹.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026c25b1cc68947286d80b8cd44bf57dbb6'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb20261d7dae9633bcd24dce8fe6694f4c01b5',b.book_id,'lesson-16','算法的一般验证','戚玲玲','宁波市海曙区古林镇中心小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-16' AND s.kind='LESSON'
WHERE b.book_code='xxkj-5-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'1acc6506d29e1927ea9090dd9b8e8d3f791bda8ced6832bfcc3933a959976818','DESIGN','《第16课 算法验证》【教学设计】','《第16课 算法验证》【教学设计】.docx','1a/1acc6506d29e1927ea9090dd9b8e8d3f791bda8ced6832bfcc3933a959976818.docx','previews/1acc6506d29e1927ea9090dd9b8e8d3f791bda8ced6832bfcc3933a959976818.pdf',NULL,NULL,0,45411,'来源：五上（海曙）/《第16课 算法的验证》教学案例9.30/《第16课 算法验证》【教学设计】.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb20261d7dae9633bcd24dce8fe6694f4c01b5'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'542919c7405c9b5ef686f3dee97bdcab911130035fc7860ae6bac3a661427e84','WORKSHEET','学习单','学习单.docx','54/542919c7405c9b5ef686f3dee97bdcab911130035fc7860ae6bac3a661427e84.docx','previews/542919c7405c9b5ef686f3dee97bdcab911130035fc7860ae6bac3a661427e84.pdf',NULL,NULL,0,51238,'来源：五上（海曙）/《第16课 算法的验证》教学案例9.30/学习单.docx','',2
FROM biz_textbook_case c WHERE c.source_key='nb20261d7dae9633bcd24dce8fe6694f4c01b5'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-3b09813195430bd3af31f9cc73815e2f90cb3421eab7e2caa01497ff6106feb4','MATERIAL','算法的智能验证-宁海县-第17课《算法的智能验证》','第17课-算法的智能验证-吴康乐-宁海县-第17课《算法的智能验证》.zip','3b/3b09813195430bd3af31f9cc73815e2f90cb3421eab7e2caa01497ff6106feb4.zip',NULL,NULL,NULL,0,12582116,'共 5 个文件，来源目录：五上（海曙）/宁海县-第17课《算法的智能验证》','解压后按文件名和包内 README 使用。',1
FROM biz_textbook_case c WHERE c.source_key='5d6790287149ffe1e2e99c638c099706'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026f05d6327f8e401bace0dc72b183863fb',b.book_id,'lesson-1','身边的系统','王巧莉','慈城中心小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-1' AND s.kind='LESSON'
WHERE b.book_code='xxkj-5-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'f92c13525196293b746b024f4fb645b5ccb9e979d668854a1938696118895d37','DESIGN','1第1课身边的系统教学设计','1第1课身边的系统教学设计.docx','f9/f92c13525196293b746b024f4fb645b5ccb9e979d668854a1938696118895d37.docx','previews/f92c13525196293b746b024f4fb645b5ccb9e979d668854a1938696118895d37.pdf',NULL,NULL,0,147726,'来源：五下（江北）/1第1课身边的系统教学设计.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026f05d6327f8e401bace0dc72b183863fb'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb202696f88283badb0a0e289448c141170ca7',b.book_id,'lesson-2','系统的构成','郑少琪','宁波大学青藤书院','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-2' AND s.kind='LESSON'
WHERE b.book_code='xxkj-5-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'5bc19e515ef9f2029fe83f0248242a2a6d0590b78a3e872e12f6bb77dc35a0b5','DESIGN','2第2课系统的构成教学设计','2第2课系统的构成教学设计.docx','5b/5bc19e515ef9f2029fe83f0248242a2a6d0590b78a3e872e12f6bb77dc35a0b5.docx','previews/5bc19e515ef9f2029fe83f0248242a2a6d0590b78a3e872e12f6bb77dc35a0b5.pdf',NULL,NULL,0,154469,'来源：五下（江北）/2第2课系统的构成教学设计.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb202696f88283badb0a0e289448c141170ca7'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb20260b596773278c1ae5b5885b3559cca46c',b.book_id,'lesson-3','观察系统','迟薇羽','宁波大学青藤书院','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-3' AND s.kind='LESSON'
WHERE b.book_code='xxkj-5-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'d573f49c46cd5e66f2058b600d84ca51b5ed5f6a9efcf74440aeaecc47e598a3','DESIGN','3第3课 观察系统教学设计','3第3课 观察系统教学设计.docx','d5/d573f49c46cd5e66f2058b600d84ca51b5ed5f6a9efcf74440aeaecc47e598a3.docx','previews/d573f49c46cd5e66f2058b600d84ca51b5ed5f6a9efcf74440aeaecc47e598a3.pdf',NULL,NULL,0,371663,'来源：五下（江北）/3第3课 观察系统教学设计.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb20260b596773278c1ae5b5885b3559cca46c'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026e5c48387984496353a06e9058ea42961',b.book_id,'lesson-4','生活中的控制系统','蒋坚','宁波大学附属学校','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-4' AND s.kind='LESSON'
WHERE b.book_code='xxkj-5-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'7432a78391da6b582d7718a9b08ee5acf09b83b25eccb628f0ea4908ad96e9f8','DESIGN','4第4课生活中的控制系统教学设计','4第4课生活中的控制系统教学设计.docx','74/7432a78391da6b582d7718a9b08ee5acf09b83b25eccb628f0ea4908ad96e9f8.docx','previews/7432a78391da6b582d7718a9b08ee5acf09b83b25eccb628f0ea4908ad96e9f8.pdf',NULL,NULL,0,85665,'来源：五下（江北）/4第4课生活中的控制系统教学设计.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026e5c48387984496353a06e9058ea42961'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026915c226109ed5dee103eaa664e21e58b',b.book_id,'lesson-5','控制系统的三个环节','陈虹','宁波大学附属学校','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-5' AND s.kind='LESSON'
WHERE b.book_code='xxkj-5-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'a9bfbc31ee983cbdbe8309993a5f99e9bf2aa7d214cf7589a20021d4b4aabd58','DESIGN','5第5课控制系统的三个环节教学设计','5第5课控制系统的三个环节教学设计.docx','a9/a9bfbc31ee983cbdbe8309993a5f99e9bf2aa7d214cf7589a20021d4b4aabd58.docx','previews/a9bfbc31ee983cbdbe8309993a5f99e9bf2aa7d214cf7589a20021d4b4aabd58.pdf',NULL,NULL,0,569258,'来源：五下（江北）/5第5课控制系统的三个环节教学设计.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026915c226109ed5dee103eaa664e21e58b'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026b14690f496cc7281e557e644850b0cf6',b.book_id,'lesson-6','控制系统的输入','周杨凯','江北区外国语艺术学校','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-6' AND s.kind='LESSON'
WHERE b.book_code='xxkj-5-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'504f046e9cfd8199ab725c29eac379355d70aac17db410a80014325355d02c2e','DESIGN','6第6课控制系统的输入教学设计','6第6课控制系统的输入教学设计.docx','50/504f046e9cfd8199ab725c29eac379355d70aac17db410a80014325355d02c2e.docx','previews/504f046e9cfd8199ab725c29eac379355d70aac17db410a80014325355d02c2e.pdf',NULL,NULL,0,464304,'来源：五下（江北）/6第6课控制系统的输入教学设计.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026b14690f496cc7281e557e644850b0cf6'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026035cc58bcb742bab8a882650a76d27fe',b.book_id,'lesson-7','控制系统的输出','李梦梦','江北区外国语艺术学校','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-7' AND s.kind='LESSON'
WHERE b.book_code='xxkj-5-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'51b1bf84ae6146215d1b5b5138cb367b4ade5c55213e028a76106948431acba5','DESIGN','7第7课控制系统的输出教学设计','7第7课控制系统的输出教学设计.docx','51/51b1bf84ae6146215d1b5b5138cb367b4ade5c55213e028a76106948431acba5.docx','previews/51b1bf84ae6146215d1b5b5138cb367b4ade5c55213e028a76106948431acba5.pdf',NULL,NULL,0,625909,'来源：五下（江北）/7第7课控制系统的输出教学设计.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026035cc58bcb742bab8a882650a76d27fe'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb202668b8bed6b8f13764c7b2b0e8044492cd',b.book_id,'lesson-8','体验控制系统','卢昱','宁波福山正达外国语学校','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-8' AND s.kind='LESSON'
WHERE b.book_code='xxkj-5-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'cbb69889c681689055a8b1bb9c09b85866679d861cfd9cbb1e4b342049824ee8','DESIGN','8第8课体验控制结构教学设计','8第8课体验控制结构教学设计.docx','cb/cbb69889c681689055a8b1bb9c09b85866679d861cfd9cbb1e4b342049824ee8.docx','previews/cbb69889c681689055a8b1bb9c09b85866679d861cfd9cbb1e4b342049824ee8.pdf',NULL,NULL,0,484405,'来源：五下（江北）/8第8课体验控制结构教学设计.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb202668b8bed6b8f13764c7b2b0e8044492cd'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026223750076a7bf635ff8be40c8559fc5b',b.book_id,'lesson-9','控制系统中的计算','徐迦勒','宁波市修人学校','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-9' AND s.kind='LESSON'
WHERE b.book_code='xxkj-5-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'d2dfeb583c0c37e9682481bf6105029c55d05f0aab1a13cf2e8129aba15489cb','DESIGN','9第9课系统中的计算教学设计','9第9课系统中的计算教学设计.docx','d2/d2dfeb583c0c37e9682481bf6105029c55d05f0aab1a13cf2e8129aba15489cb.docx','previews/d2dfeb583c0c37e9682481bf6105029c55d05f0aab1a13cf2e8129aba15489cb.pdf',NULL,NULL,0,283917,'来源：五下（江北）/9第9课系统中的计算教学设计.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026223750076a7bf635ff8be40c8559fc5b'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026646370f9df0681f2201ea1f9944f3eb8',b.book_id,'lesson-10','计算机在控制系统中的作用','顾碧婷','江北区新城外国语学校','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-10' AND s.kind='LESSON'
WHERE b.book_code='xxkj-5-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'afc9635c41614d143396fb3a64d9cc2d84d9a8417638b75cf20cdbb0695c8e0b','DESIGN','10第10课 计算机在控制系统中的作用 教学设计','10第10课 计算机在控制系统中的作用 教学设计.docx','af/afc9635c41614d143396fb3a64d9cc2d84d9a8417638b75cf20cdbb0695c8e0b.docx','previews/afc9635c41614d143396fb3a64d9cc2d84d9a8417638b75cf20cdbb0695c8e0b.pdf',NULL,NULL,0,504867,'来源：五下（江北）/10第10课 计算机在控制系统中的作用 教学设计.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026646370f9df0681f2201ea1f9944f3eb8'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb20263539fae3b7c0162b28c919b740e60ba3',b.book_id,'lesson-11','查表计算','陈思瑞','江北中心学校','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-11' AND s.kind='LESSON'
WHERE b.book_code='xxkj-5-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'577600e07d94e14e95db0453551ad2d348de1282487ae1fecebc62271318d43b','DESIGN','11第11课查表计算教学设计','11第11课查表计算教学设计.docx','57/577600e07d94e14e95db0453551ad2d348de1282487ae1fecebc62271318d43b.docx','previews/577600e07d94e14e95db0453551ad2d348de1282487ae1fecebc62271318d43b.pdf',NULL,NULL,0,294389,'来源：五下（江北）/11第11课查表计算教学设计.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb20263539fae3b7c0162b28c919b740e60ba3'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-6b29182a5c36525ace26edd7602a772b09307b0f13602b9cd7ec92ba430164cf','MATERIAL','条件计算-奉化区-第12课《条件计算》','第12课-条件计算-仇蓓蓓-奉化区-第12课《条件计算》.zip','6b/6b29182a5c36525ace26edd7602a772b09307b0f13602b9cd7ec92ba430164cf.zip',NULL,NULL,NULL,0,138024,'共 3 个文件，来源目录：五下（江北）/奉化区-第12课《条件计算》','解压后用浏览器打开入口 HTML；若页面依赖本地服务或模型文件，请按包内 README 启动。',1
FROM biz_textbook_case c WHERE c.source_key='da0fe7663f52acffca2a6a294ca46e94'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026fc1f9a7e44b9ee8d685663175ec9a0cb',b.book_id,'lesson-12','条件计算','吕贝','宁波市中城小学','NINGBO2026','PUBLISHED',102
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-12' AND s.kind='LESSON'
WHERE b.book_code='xxkj-5-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'190f24b18386756df953b51a76aec0cdb7887cf5fccdfd7af03cde4688af3e7a','DESIGN','12第12课 条件计算 教学设计','12第12课 条件计算 教学设计.docx','19/190f24b18386756df953b51a76aec0cdb7887cf5fccdfd7af03cde4688af3e7a.docx','previews/190f24b18386756df953b51a76aec0cdb7887cf5fccdfd7af03cde4688af3e7a.pdf',NULL,NULL,0,414529,'来源：五下（江北）/12第12课 条件计算 教学设计.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026fc1f9a7e44b9ee8d685663175ec9a0cb'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb20261ed9f0388052b65619e1a77288b520b3',b.book_id,'lesson-13','循环结构（一）','张杰','宁波福山正达外国语学校','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-13' AND s.kind='LESSON'
WHERE b.book_code='xxkj-5-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'788adadf834cd5caab5620ff0d349bb18eedda55da74ac5d8f1ebcde301dd381','DESIGN','13第13课循环结构（一）教学设计','13第13课循环结构（一）教学设计.docx','78/788adadf834cd5caab5620ff0d349bb18eedda55da74ac5d8f1ebcde301dd381.docx','previews/788adadf834cd5caab5620ff0d349bb18eedda55da74ac5d8f1ebcde301dd381.pdf',NULL,NULL,0,109723,'来源：五下（江北）/13第13课循环结构（一）教学设计.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb20261ed9f0388052b65619e1a77288b520b3'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb20262251669787398baa13d471c3492831d7',b.book_id,'lesson-14','循环结构（二）','职笑','中城小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-14' AND s.kind='LESSON'
WHERE b.book_code='xxkj-5-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'899b4a007ec43f52604c5e234b5ce4b5afb95a9a298bd80b7e25b29cead45b08','DESIGN','14第14课循环结构（二）教学设计','14第14课循环结构（二）教学设计.docx','89/899b4a007ec43f52604c5e234b5ce4b5afb95a9a298bd80b7e25b29cead45b08.docx','previews/899b4a007ec43f52604c5e234b5ce4b5afb95a9a298bd80b7e25b29cead45b08.pdf',NULL,NULL,0,377063,'来源：五下（江北）/14第14课循环结构（二）教学设计.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb20262251669787398baa13d471c3492831d7'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb202683e90ef6cd168ecbb650f2b0fc458647',b.book_id,'lesson-15','恒温箱实验','叶红霞','洪塘中心小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-15' AND s.kind='LESSON'
WHERE b.book_code='xxkj-5-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'22030e91865d85dd9a8f8a18bbec3d9df0aac5faee334cc0733fdb4b372975a8','DESIGN','15第15课恒温箱实验教学设计','15第15课恒温箱实验教学设计.docx','22/22030e91865d85dd9a8f8a18bbec3d9df0aac5faee334cc0733fdb4b372975a8.docx','previews/22030e91865d85dd9a8f8a18bbec3d9df0aac5faee334cc0733fdb4b372975a8.pdf',NULL,NULL,0,529727,'来源：五下（江北）/15第15课恒温箱实验教学设计.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb202683e90ef6cd168ecbb650f2b0fc458647'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-f0929a9c59878b875fce7487228984fe05b8677cbbaac0665eb3a61d3ae75097','MATERIAL','恒温箱实验-奉化区-第15课《恒温箱实验》','第15课-恒温箱实验-张红波-奉化区-第15课《恒温箱实验》.zip','f0/f0929a9c59878b875fce7487228984fe05b8677cbbaac0665eb3a61d3ae75097.zip',NULL,NULL,NULL,0,988,'共 1 个文件，来源目录：五下（江北）/奉化区-第15课《恒温箱实验》','解压后按文件名和包内 README 使用。',1
FROM biz_textbook_case c WHERE c.source_key='26cb086c53e3f34666b32aaa3a7b8975'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-8477f78fcdf8103a5f06f1bd38d2fbb2b8537e761c1b376622e0c60c13021330','MATERIAL','认识神经网络-奉化区-第16课《认识神经网络》(1)','第16课-认识神经网络-姚新淼-奉化区-第16课《认识神经网络》(1).zip','84/8477f78fcdf8103a5f06f1bd38d2fbb2b8537e761c1b376622e0c60c13021330.zip',NULL,NULL,NULL,0,390906,'共 3 个文件，来源目录：五下（江北）/奉化区-第16课《认识神经网络》(1)','解压后用浏览器打开入口 HTML；若页面依赖本地服务或模型文件，请按包内 README 启动。',1
FROM biz_textbook_case c WHERE c.source_key='5fd3b3eb683e32cca3946316d4827a5b'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-d909347683669464c1f2a264bcaf4da637c77a879bb33b8c2e7e2b959287d98f','MATERIAL','用神经网络进行分类-奉化区-第17课《利用神经网络进行分类》(1)','第17课-用神经网络进行分类-傅迪-奉化区-第17课《利用神经网络进行分类》(1).zip','d9/d909347683669464c1f2a264bcaf4da637c77a879bb33b8c2e7e2b959287d98f.zip',NULL,NULL,NULL,0,635184,'共 7 个文件，来源目录：五下（江北）/奉化区-第17课《利用神经网络进行分类》(1)','解压后按文件名和包内 README 使用。',1
FROM biz_textbook_case c WHERE c.source_key='4c85b3ea4118212e2f655decb4ff2a26'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-5825425f719c7c7dbe8330bcc3bf187377111c567daf3ef25b9e3b8355a64f21','MATERIAL','用神经网络进行预测-奉化区-第18课《用神经网络进行预测》','第18课-用神经网络进行预测-袁立玺-奉化区-第18课《用神经网络进行预测》.zip','58/5825425f719c7c7dbe8330bcc3bf187377111c567daf3ef25b9e3b8355a64f21.zip',NULL,NULL,NULL,0,24347,'共 1 个文件，来源目录：五下（江北）/奉化区-第18课《用神经网络进行预测》','解压后用浏览器打开入口 HTML；若页面依赖本地服务或模型文件，请按包内 README 启动。',1
FROM biz_textbook_case c WHERE c.source_key='57e955c478996d357ed6beb18d66bfee'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026f4254dc9007345256edf07581059dea4',b.book_id,'lesson-1','算法与问题解决','闻凯燕','慈溪市龙山镇实验小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-1' AND s.kind='LESSON'
WHERE b.book_code='xxkj-6-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'e93977ab3aa0988c4c26ed1394de46b5cd4d5e7a3de8dbd8a367c48167dae9c3','DESIGN','第1课 算法与问题解决','第1课 算法与问题解决.docx','e9/e93977ab3aa0988c4c26ed1394de46b5cd4d5e7a3de8dbd8a367c48167dae9c3.docx','previews/e93977ab3aa0988c4c26ed1394de46b5cd4d5e7a3de8dbd8a367c48167dae9c3.pdf',NULL,NULL,0,197122,'来源：六上（慈溪）/第1课 算法与问题解决.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026f4254dc9007345256edf07581059dea4'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026de4cba82733305214745cda64edc5266',b.book_id,'lesson-2','抽象与建模','','','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-2' AND s.kind='LESSON'
WHERE b.book_code='xxkj-6-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'c85629f8ed6cf0a648947387158607731bb728395460de9b71852ce49907a1cd','DESIGN','第2课 抽象与建模','第2课 抽象与建模.docx','c8/c85629f8ed6cf0a648947387158607731bb728395460de9b71852ce49907a1cd.docx','previews/c85629f8ed6cf0a648947387158607731bb728395460de9b71852ce49907a1cd.pdf',NULL,NULL,0,86648,'来源：六上（慈溪）/第2课 抽象与建模.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026de4cba82733305214745cda64edc5266'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb20266e3ffc14439333302327f0d9d20eb978',b.book_id,'lesson-3','算法设计','孙范盛','慈溪市附海镇中心小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-3' AND s.kind='LESSON'
WHERE b.book_code='xxkj-6-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'62c685d45cbbe63386bb5be1c8d96342c7132c4a1a3ed68e0ad1e34327c65dce','DESIGN','第3课 算法设计','第3课 算法设计.docx','62/62c685d45cbbe63386bb5be1c8d96342c7132c4a1a3ed68e0ad1e34327c65dce.docx','previews/62c685d45cbbe63386bb5be1c8d96342c7132c4a1a3ed68e0ad1e34327c65dce.pdf',NULL,NULL,0,2346467,'来源：六上（慈溪）/第3课 算法设计.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb20266e3ffc14439333302327f0d9d20eb978'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026430b664483324719e67645f453c49340',b.book_id,'lesson-4','用程序设计语言描述算法','孙红萍','慈溪市桥头镇实验学校','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-4' AND s.kind='LESSON'
WHERE b.book_code='xxkj-6-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'dd531e12f221b12c2f2bcf023e34287a7815641c260dfcb1b7ea86922364d61c','DESIGN','第4课 算法的程序体验','第4课 算法的程序体验.docx','dd/dd531e12f221b12c2f2bcf023e34287a7815641c260dfcb1b7ea86922364d61c.docx','previews/dd531e12f221b12c2f2bcf023e34287a7815641c260dfcb1b7ea86922364d61c.pdf',NULL,NULL,0,64013,'来源：六上（慈溪）/第4课 算法的程序体验.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026430b664483324719e67645f453c49340'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb202699c93a29b4d551ba05ab8350582ea4f7',b.book_id,'lesson-5','算法的执行','李波 高荣','慈溪市匡堰实验学校','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-5' AND s.kind='LESSON'
WHERE b.book_code='xxkj-6-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'5629267afab763f21675ebbe0497bacb55ec0b664f2e5f2702998a95d8123c87','DESIGN','第5课 算法的执行','第5课 算法的执行.docx','56/5629267afab763f21675ebbe0497bacb55ec0b664f2e5f2702998a95d8123c87.docx','previews/5629267afab763f21675ebbe0497bacb55ec0b664f2e5f2702998a95d8123c87.pdf',NULL,NULL,0,544818,'来源：六上（慈溪）/第5课 算法的执行.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb202699c93a29b4d551ba05ab8350582ea4f7'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026f69b0c70c05f21ff1e3481530b936ea2',b.book_id,'lesson-6','“猜数字”算法设计','张积','慈溪市逍林镇中心小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-6' AND s.kind='LESSON'
WHERE b.book_code='xxkj-6-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'ea118a1cb37828f792a20b926c989386db5c5497d7020456a095774ca5c1c27b','DESIGN','第6课 猜数字算法设计','第6课 猜数字算法设计.docx','ea/ea118a1cb37828f792a20b926c989386db5c5497d7020456a095774ca5c1c27b.docx','previews/ea118a1cb37828f792a20b926c989386db5c5497d7020456a095774ca5c1c27b.pdf',NULL,NULL,0,23019,'来源：六上（慈溪）/第6课 猜数字算法设计.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026f69b0c70c05f21ff1e3481530b936ea2'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-b9b28b335ef4e881be335e8f73e1c8da9c18ac793016dad8b5b08b26acf20867','MATERIAL','用人工智能辅助算法实现-第7课《用人工智能辅助算法实现》-宁波市镇海区蛟川中心学校-王素军','第7课-用人工智能辅助算法实现-王素军-第7课《用人工智能辅助算法实现》-宁波市镇海区蛟川中心学校-王素军.zip','b9/b9b28b335ef4e881be335e8f73e1c8da9c18ac793016dad8b5b08b26acf20867.zip',NULL,NULL,NULL,0,434992,'共 2 个文件，来源目录：六上（慈溪）/第7课《用人工智能辅助算法实现》-宁波市镇海区蛟川中心学校-王素军','解压后用浏览器打开入口 HTML；若页面依赖本地服务或模型文件，请按包内 README 启动。',1
FROM biz_textbook_case c WHERE c.source_key='dacff978b138264c4e3072bf495c3fed'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026474da337e000e5237006687f69518e3d',b.book_id,'lesson-8','算法的多样性','','','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-8' AND s.kind='LESSON'
WHERE b.book_code='xxkj-6-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'2f7533252b5605a513579a4146c05089396f02493bf1e8d4c4813149539a9479','DESIGN','第8课 算法的多样性','第8课 算法的多样性.docx','2f/2f7533252b5605a513579a4146c05089396f02493bf1e8d4c4813149539a9479.docx','previews/2f7533252b5605a513579a4146c05089396f02493bf1e8d4c4813149539a9479.pdf',NULL,NULL,0,159268,'来源：六上（慈溪）/第8课 算法的多样性.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026474da337e000e5237006687f69518e3d'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb20262340a30d7aa0186592d5de8075e9993f',b.book_id,'lesson-9','算法的评价方法','朱孙杰','慈溪市横河镇中心小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-9' AND s.kind='LESSON'
WHERE b.book_code='xxkj-6-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'7a1e820b56f47ffa65f15a7cefdc32c38a0e3424f15376c6fe9faeed08592413','DESIGN','第9课 算法的评价方法','第9课 算法的评价方法.docx','7a/7a1e820b56f47ffa65f15a7cefdc32c38a0e3424f15376c6fe9faeed08592413.docx','previews/7a1e820b56f47ffa65f15a7cefdc32c38a0e3424f15376c6fe9faeed08592413.pdf',NULL,NULL,0,522877,'来源：六上（慈溪）/第9课 算法的评价方法.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb20262340a30d7aa0186592d5de8075e9993f'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026f1d6ecd028d415b2b2ab0a034e0e5ff7',b.book_id,'lesson-10','“韩信点兵”枚举法的实现','马恩立','慈溪市保德实验学校','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-10' AND s.kind='LESSON'
WHERE b.book_code='xxkj-6-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'100725cf3334737207e2ff88f8ab99af135dcd2a742b91cf42b2c748b8ad2be6','DESIGN','第10课 “韩信点兵”枚举法的实现','第10课 “韩信点兵”枚举法的实现.docx','10/100725cf3334737207e2ff88f8ab99af135dcd2a742b91cf42b2c748b8ad2be6.docx','previews/100725cf3334737207e2ff88f8ab99af135dcd2a742b91cf42b2c748b8ad2be6.pdf',NULL,NULL,0,22645,'来源：六上（慈溪）/第10课 “韩信点兵”枚举法的实现.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026f1d6ecd028d415b2b2ab0a034e0e5ff7'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026fce1fdf4ae0b9d5dbb5de6ba4157cbfd',b.book_id,'lesson-11','“韩信点兵”筛选法的实现','许天鹰','慈溪市宗汉街道新界学校','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-11' AND s.kind='LESSON'
WHERE b.book_code='xxkj-6-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'49147f7d2322a1a52a52e58ed769dcdf4f010f22411576a56e5890024f7eab67','DESIGN','第11课 “韩信点兵”筛选法的实现','第11课 “韩信点兵”筛选法的实现.docx','49/49147f7d2322a1a52a52e58ed769dcdf4f010f22411576a56e5890024f7eab67.docx','previews/49147f7d2322a1a52a52e58ed769dcdf4f010f22411576a56e5890024f7eab67.pdf',NULL,NULL,0,748433,'来源：六上（慈溪）/第11课 “韩信点兵”筛选法的实现.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026fce1fdf4ae0b9d5dbb5de6ba4157cbfd'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-76fac35d39fc00749b6ef554c6646e519f63b08ff087dc1043302104368eaf3f','MATERIAL','用人工智能解决“韩信点兵”问题-第12课《用人工智能解决“韩信点兵”问题》-宁波市镇海区实验小学-章晓伟','第12课-用人工智能解决“韩信点兵”问题-章晓伟-第12课《用人工智能解决“韩信点兵”问题》-宁波市镇海区实验小学-章晓伟.zip','76/76fac35d39fc00749b6ef554c6646e519f63b08ff087dc1043302104368eaf3f.zip',NULL,NULL,NULL,0,22011,'共 5 个文件，来源目录：六上（慈溪）/第12课《用人工智能解决“韩信点兵”问题》-宁波市镇海区实验小学-章晓伟','解压后用浏览器打开入口 HTML；若页面依赖本地服务或模型文件，请按包内 README 启动。',1
FROM biz_textbook_case c WHERE c.source_key='eb30d1e4bb6897453d4984e132199d06'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026c4ed01a1a489987a3b81d45976538e17',b.book_id,'lesson-13','在线生活中的算法','','','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-13' AND s.kind='LESSON'
WHERE b.book_code='xxkj-6-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'208e15ef8112e5cff4e27f94dc1d45f24132928134798625acf823c8b8298db2','ARCHIVE','第13课 在线生活中的算法','第13课 在线生活中的算法.zip','20/208e15ef8112e5cff4e27f94dc1d45f24132928134798625acf823c8b8298db2.zip',NULL,NULL,NULL,0,51161689,'来源：六上（慈溪）/第13课 在线生活中的算法.zip','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026c4ed01a1a489987a3b81d45976538e17'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-90ed0e9924304c7e6419d20058e1a8c0f33da4705aaae735687fc65f8f584a5d','MATERIAL','智能寻路中的搜索算法-第14课《智能寻路中的搜索算法》-宁波市镇海区崇正书院-林珠','第14课-智能寻路中的搜索算法-林珠-第14课《智能寻路中的搜索算法》-宁波市镇海区崇正书院-林珠.zip','90/90ed0e9924304c7e6419d20058e1a8c0f33da4705aaae735687fc65f8f584a5d.zip',NULL,NULL,NULL,0,27838,'共 4 个文件，来源目录：六上（慈溪）/第14课《智能寻路中的搜索算法》-宁波市镇海区崇正书院-林珠','解压后用浏览器打开入口 HTML；若页面依赖本地服务或模型文件，请按包内 README 启动。',1
FROM biz_textbook_case c WHERE c.source_key='25e2a6ab5a8b8ce66ea4ab176f5ee059'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-2702d55ac4ad812b013a4799b44ccd5a904eed77507f85b89097524b487da066','MATERIAL','在线购物中的推荐算法-第15课《在线购物中的推荐算法》-镇海区静远小学-周健','第15课-在线购物中的推荐算法-周健-第15课《在线购物中的推荐算法》-镇海区静远小学-周健.zip','27/2702d55ac4ad812b013a4799b44ccd5a904eed77507f85b89097524b487da066.zip',NULL,NULL,NULL,0,20426395,'共 15 个文件，来源目录：六上（慈溪）/第15课《在线购物中的推荐算法》-镇海区静远小学-周健','解压后用浏览器打开入口 HTML；若页面依赖本地服务或模型文件，请按包内 README 启动。',1
FROM biz_textbook_case c WHERE c.source_key='eecd6289fc462a03d48de9acedf78483'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026eba77f89397f4acb3bf89c9b7af780d9',b.book_id,'lesson-16','算法对生活的影响','胡央科','慈溪市城区中心小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-16' AND s.kind='LESSON'
WHERE b.book_code='xxkj-6-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'0ecb3a04edb589177108b36af40c425102ff88d5b76fde92868217bc6ca4e09d','DESIGN','第16课 算法对生活的影响','第16课 算法对生活的影响.docx','0e/0ecb3a04edb589177108b36af40c425102ff88d5b76fde92868217bc6ca4e09d.docx','previews/0ecb3a04edb589177108b36af40c425102ff88d5b76fde92868217bc6ca4e09d.pdf',NULL,NULL,0,129895,'来源：六上（慈溪）/第16课 算法对生活的影响.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026eba77f89397f4acb3bf89c9b7af780d9'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026aefefabf21f5bf2b246a0d17d9a3d80e',b.book_id,'lesson-17','人机对话的实现','胡挺','慈溪市第三实验小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-17' AND s.kind='LESSON'
WHERE b.book_code='xxkj-6-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'82a9fda759ca53551e0af11a0438817d413c9a883ec6e2a4e9ce9453d2f10c1b','DESIGN','第17课 人机对话的实现','第17课 人机对话的实现.docx','82/82a9fda759ca53551e0af11a0438817d413c9a883ec6e2a4e9ce9453d2f10c1b.docx','previews/82a9fda759ca53551e0af11a0438817d413c9a883ec6e2a4e9ce9453d2f10c1b.pdf',NULL,NULL,0,32995,'来源：六上（慈溪）/第17课 人机对话的实现.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026aefefabf21f5bf2b246a0d17d9a3d80e'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-c6fb8307dac8071823e51bc43846539a8908e4a0fb9fa7fe3c4c48ae0d5b9263','MATERIAL','对话机器人-第18课《对话机器人》','第18课-对话机器人-张维春-第18课《对话机器人》.zip','c6/c6fb8307dac8071823e51bc43846539a8908e4a0fb9fa7fe3c4c48ae0d5b9263.zip',NULL,NULL,NULL,0,896789,'共 1 个文件，来源目录：六上（慈溪）/第18课《对话机器人》','解压后用浏览器打开入口 HTML；若页面依赖本地服务或模型文件，请按包内 README 启动。',1
FROM biz_textbook_case c WHERE c.source_key='c8dce0ac38d1e4f1659cccaf8f82bbc7'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-4925f8341752cd943f1ea19d72e5336e67c778345fd914699ef337996586dcf5','MATERIAL','对话机器人-第18课《对话机器人》','第18课-对话机器人-杨滨-第18课《对话机器人》.zip','49/4925f8341752cd943f1ea19d72e5336e67c778345fd914699ef337996586dcf5.zip',NULL,NULL,NULL,0,104035,'共 4 个文件，来源目录：六上（慈溪）/第18课《对话机器人》-宁波市镇海区中心学校-杨滨','解压后用浏览器打开入口 HTML；若页面依赖本地服务或模型文件，请按包内 README 启动。',1
FROM biz_textbook_case c WHERE c.source_key='b5519c6a829a8bd6e96e4d2157d9cf0f'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026e1a8d7e570dc76a0613f3c29a05eee90',b.book_id,'lesson-1','自动控制系统','黄乐帅','余姚市泗门镇中心小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-1' AND s.kind='LESSON'
WHERE b.book_code='xxkj-6-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'b34af35f248928139f1d9580ed34db41f39bffd8c32a68ba73df149701f873eb','DESIGN','1.自动控制系统','1.自动控制系统.docx','b3/b34af35f248928139f1d9580ed34db41f39bffd8c32a68ba73df149701f873eb.docx','previews/b34af35f248928139f1d9580ed34db41f39bffd8c32a68ba73df149701f873eb.pdf',NULL,NULL,0,94425,'来源：六下（余姚）/1.自动控制系统.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026e1a8d7e570dc76a0613f3c29a05eee90'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb20261fd11d67a90f63c5de77ae2d8ead463d',b.book_id,'lesson-2','控制的形态','姚静','余姚市泗门镇第二小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-2' AND s.kind='LESSON'
WHERE b.book_code='xxkj-6-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'71c962ee6f01ac2c3295dc09106a96d7c9717e49b48eb82ba66def6b5b5b0a9b','DESIGN','2.控制的形态','2.控制的形态.docx','71/71c962ee6f01ac2c3295dc09106a96d7c9717e49b48eb82ba66def6b5b5b0a9b.docx','previews/71c962ee6f01ac2c3295dc09106a96d7c9717e49b48eb82ba66def6b5b5b0a9b.pdf',NULL,NULL,0,179784,'来源：六下（余姚）/2.控制的形态.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb20261fd11d67a90f63c5de77ae2d8ead463d'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb20260c8720c9f149a4d648b16712e103aad9',b.book_id,'lesson-3','反馈与控制','许惊鸿','宁波上海世界外国语学校','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-3' AND s.kind='LESSON'
WHERE b.book_code='xxkj-6-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'c4548d1a5dbe686e7685fbf1fd4dae22df757db71e06f7f52247213617640a89','DESIGN','3.反馈与控制','3.反馈与控制.docx','c4/c4548d1a5dbe686e7685fbf1fd4dae22df757db71e06f7f52247213617640a89.docx','previews/c4548d1a5dbe686e7685fbf1fd4dae22df757db71e06f7f52247213617640a89.pdf',NULL,NULL,0,292193,'来源：六下（余姚）/3.反馈与控制.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb20260c8720c9f149a4d648b16712e103aad9'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb202655902ed30eeb1a15cd76abd97f5b5ebe',b.book_id,'lesson-4','反馈的实现','童俊杰','余姚市瑞云学校','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-4' AND s.kind='LESSON'
WHERE b.book_code='xxkj-6-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'789f18a2c46a2dbf21cfa03ea33709e83512470b0a0009cf51dfb28dfa9b0dcb','DESIGN','4.反馈的实现','4.反馈的实现.docx','78/789f18a2c46a2dbf21cfa03ea33709e83512470b0a0009cf51dfb28dfa9b0dcb.docx','previews/789f18a2c46a2dbf21cfa03ea33709e83512470b0a0009cf51dfb28dfa9b0dcb.pdf',NULL,NULL,0,98964,'来源：六下（余姚）/4.反馈的实现.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb202655902ed30eeb1a15cd76abd97f5b5ebe'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb202662586858ac0c2d0993f3ab5b06404f25',b.book_id,'lesson-5','控制系统中的数据','洪陈丽','余姚市姚江小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-5' AND s.kind='LESSON'
WHERE b.book_code='xxkj-6-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'1d6cc2ab3ff81983c3b178ff2c212cea2f5372eee2c37fa1fa5d709d6e638e12','MATERIAL','5.控制系统中的数据','5.控制系统中的数据.docx','1d/1d6cc2ab3ff81983c3b178ff2c212cea2f5372eee2c37fa1fa5d709d6e638e12.docx','previews/1d6cc2ab3ff81983c3b178ff2c212cea2f5372eee2c37fa1fa5d709d6e638e12.pdf',NULL,NULL,0,329392,'来源：六下（余姚）/5.控制系统中的数据.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb202662586858ac0c2d0993f3ab5b06404f25'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb202623051527000dfbdf4b0f41179279eb25',b.book_id,'lesson-6','连续量与开关量','教育集团','余姚市东风小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-6' AND s.kind='LESSON'
WHERE b.book_code='xxkj-6-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'8bd66ecb519d054a78f4ddcea0b2d8b6661c17eaaad81351040039195ac9540f','DESIGN','6.连续量与开关量','6.连续量与开关量.docx','8b/8bd66ecb519d054a78f4ddcea0b2d8b6661c17eaaad81351040039195ac9540f.docx','previews/8bd66ecb519d054a78f4ddcea0b2d8b6661c17eaaad81351040039195ac9540f.pdf',NULL,NULL,0,584384,'来源：六下（余姚）/6.连续量与开关量.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb202623051527000dfbdf4b0f41179279eb25'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-a2ad58258808abbea8b5e33e0e7ac081c43331db1d5883bdf2dbf455dbaffd51','MATERIAL','开关量的生成-第7课《开关量的生成》-余姚东风小学教育集团东江校区-侯娜飞','第7课-开关量的生成-侯娜飞-第7课《开关量的生成》-余姚东风小学教育集团东江校区-侯娜飞.zip','a2/a2ad58258808abbea8b5e33e0e7ac081c43331db1d5883bdf2dbf455dbaffd51.zip',NULL,NULL,NULL,0,470172,'共 1 个文件，来源目录：六下（余姚）/第7课《开关量的生成》-余姚东风小学教育集团东江校区-侯娜飞','解压后按文件名和包内 README 使用。',1
FROM biz_textbook_case c WHERE c.source_key='5c2713d0a2bbcfe157b8385552ad4558'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026fccd680402951e295ee96b45e6aebdaf',b.book_id,'lesson-7','开关量的生成','王晓珍','宁波市江北区实验小学','NINGBO2026','PUBLISHED',102
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-7' AND s.kind='LESSON'
WHERE b.book_code='xxkj-6-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'218fed7c4bde7a37dfe49496f90373ef9d02d50a439f3aceb343c02040e5f8a4','DESIGN','7.开关量的生成','7.开关量的生成.docx','21/218fed7c4bde7a37dfe49496f90373ef9d02d50a439f3aceb343c02040e5f8a4.docx','previews/218fed7c4bde7a37dfe49496f90373ef9d02d50a439f3aceb343c02040e5f8a4.pdf',NULL,NULL,0,602501,'来源：六下（余姚）/7.开关量的生成.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026fccd680402951e295ee96b45e6aebdaf'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026608f3bf50f945549f2901fb1f303a1dc',b.book_id,'lesson-8','开关量的与运算','沈菲菲 李炯荣','余姚市梨洲小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-8' AND s.kind='LESSON'
WHERE b.book_code='xxkj-6-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'471762fa34a465143f5ea98df69b83bc59b8259addea201220b25fd86013570d','DESIGN','8.开关量的与运算','8.开关量的与运算.docx','47/471762fa34a465143f5ea98df69b83bc59b8259addea201220b25fd86013570d.docx','previews/471762fa34a465143f5ea98df69b83bc59b8259addea201220b25fd86013570d.pdf',NULL,NULL,0,59436,'来源：六下（余姚）/8.开关量的与运算.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026608f3bf50f945549f2901fb1f303a1dc'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026db64940419da2088524328577bb9f45a',b.book_id,'lesson-9','开关量的或运算','教育集团','余姚市东风小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-9' AND s.kind='LESSON'
WHERE b.book_code='xxkj-6-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'a39f0a015742ef4110945e2d50524b6c1b9f81ce69196994b2b83b0c4e308475','DESIGN','9.开关量的或运算','9.开关量的或运算.docx','a3/a39f0a015742ef4110945e2d50524b6c1b9f81ce69196994b2b83b0c4e308475.docx','previews/a39f0a015742ef4110945e2d50524b6c1b9f81ce69196994b2b83b0c4e308475.pdf',NULL,NULL,0,234019,'来源：六下（余姚）/9.开关量的或运算.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026db64940419da2088524328577bb9f45a'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb202648136fceef1fbe7dcaf6c73918a5b963',b.book_id,'lesson-10','开关量与控制','柴国芸','余姚市第二实验小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-10' AND s.kind='LESSON'
WHERE b.book_code='xxkj-6-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'69023aa809d3b329c9a5846e4ea3c39346410e1815bbc61e3ec4d71a2a43956c','DESIGN','10.开关量与控制','10.开关量与控制.docx','69/69023aa809d3b329c9a5846e4ea3c39346410e1815bbc61e3ec4d71a2a43956c.docx','previews/69023aa809d3b329c9a5846e4ea3c39346410e1815bbc61e3ec4d71a2a43956c.pdf',NULL,NULL,0,133440,'来源：六下（余姚）/10.开关量与控制.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb202648136fceef1fbe7dcaf6c73918a5b963'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026fa4bed0f0eb2244da32eb4a9ff44c03e',b.book_id,'lesson-11','身边的扩音系统','丁一璐','余姚市东城小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-11' AND s.kind='LESSON'
WHERE b.book_code='xxkj-6-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'36c9162d6405327f0ed41f2bfaffe0b841d2133752042ceb226e564bbc5009e0','DESIGN','11.身边的扩音系统','11.身边的扩音系统.docx','36/36c9162d6405327f0ed41f2bfaffe0b841d2133752042ceb226e564bbc5009e0.docx','previews/36c9162d6405327f0ed41f2bfaffe0b841d2133752042ceb226e564bbc5009e0.pdf',NULL,NULL,0,222975,'来源：六下（余姚）/11.身边的扩音系统.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026fa4bed0f0eb2244da32eb4a9ff44c03e'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026bbf1202d0961fcfd2d9554f96655c7b7',b.book_id,'lesson-12','计算机控制的扩音系统','史佳莹','余姚市姚北实验学校','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-12' AND s.kind='LESSON'
WHERE b.book_code='xxkj-6-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'5f228e9c232ca2b61cc1c8809e0a70a59ca73ae538f67f94bd08364f08195918','DESIGN','12.计算机控制的扩音系统','12.计算机控制的扩音系统.docx','5f/5f228e9c232ca2b61cc1c8809e0a70a59ca73ae538f67f94bd08364f08195918.docx','previews/5f228e9c232ca2b61cc1c8809e0a70a59ca73ae538f67f94bd08364f08195918.pdf',NULL,NULL,0,81481,'来源：六下（余姚）/12.计算机控制的扩音系统.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026bbf1202d0961fcfd2d9554f96655c7b7'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026f1fe17086cbc3181ef6c6ce4aca4f63b',b.book_id,'lesson-13','扩音系统的控制','邹琦','余姚市临山镇中心小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-13' AND s.kind='LESSON'
WHERE b.book_code='xxkj-6-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'509265f213ca52c5983b343937231d62948f03af0116549ab32e1d0d04b192d8','DESIGN','13.扩音系统的控制','13.扩音系统的控制.docx','50/509265f213ca52c5983b343937231d62948f03af0116549ab32e1d0d04b192d8.docx','previews/509265f213ca52c5983b343937231d62948f03af0116549ab32e1d0d04b192d8.pdf',NULL,NULL,0,105776,'来源：六下（余姚）/13.扩音系统的控制.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026f1fe17086cbc3181ef6c6ce4aca4f63b'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026622b772bece202b21b2901b72d325ea4',b.book_id,'lesson-14','扩音系统的优化','龚圣星','余姚市更大小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-14' AND s.kind='LESSON'
WHERE b.book_code='xxkj-6-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'385dd378035e543e8748d5bd744a59913791a6a811b501f24e6e813daca1636b','DESIGN','14.扩音系统的优化','14.扩音系统的优化.docx','38/385dd378035e543e8748d5bd744a59913791a6a811b501f24e6e813daca1636b.docx','previews/385dd378035e543e8748d5bd744a59913791a6a811b501f24e6e813daca1636b.pdf',NULL,NULL,0,414778,'来源：六下（余姚）/14.扩音系统的优化.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026622b772bece202b21b2901b72d325ea4'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb20266d9c4fa6740c77aea9014f8233d2b052',b.book_id,'lesson-15','控制系统的安全','罗艺','余姚市三七市镇二六市小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-15' AND s.kind='LESSON'
WHERE b.book_code='xxkj-6-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'8c270085003c2a68ade619c85a56516d5f41db25c26a5fce33ea37a56aeda0da','DESIGN','15.控制系统的安全','15.控制系统的安全.docx','8c/8c270085003c2a68ade619c85a56516d5f41db25c26a5fce33ea37a56aeda0da.docx','previews/8c270085003c2a68ade619c85a56516d5f41db25c26a5fce33ea37a56aeda0da.pdf',NULL,NULL,0,77836,'来源：六下（余姚）/15.控制系统的安全.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb20266d9c4fa6740c77aea9014f8233d2b052'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-f97d6dfe14723eb6fbd87c5aa0c7d5e5639f6061b2c1608ac43082fa6acba068','MATERIAL','控制系统的安全-余姚市-第15课《控制系统的安全》','第15课-控制系统的安全-黄乐帅-余姚市-第15课《控制系统的安全》.zip','f9/f97d6dfe14723eb6fbd87c5aa0c7d5e5639f6061b2c1608ac43082fa6acba068.zip',NULL,NULL,NULL,0,427642,'共 1 个文件，来源目录：六下（余姚）/余姚市-第15课《控制系统的安全》','解压后按文件名和包内 README 使用。',1
FROM biz_textbook_case c WHERE c.source_key='d79448e62a4c2b73058b793c31bd74cd'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026dbd87d88530850e49b019772ebc04460',b.book_id,'lesson-16','认识智能控制系统','姚静','余姚市兰江小学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-16' AND s.kind='LESSON'
WHERE b.book_code='xxkj-6-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'ba43771c7e6af788c6eb7701b2bb34302d58fd313a4cd7e1fe06890a2befd3e9','VIDEO','video-carry','video-carry.mp4','ba/ba43771c7e6af788c6eb7701b2bb34302d58fd313a4cd7e1fe06890a2befd3e9.mp4','ba/ba43771c7e6af788c6eb7701b2bb34302d58fd313a4cd7e1fe06890a2befd3e9.mp4',NULL,NULL,0,12066971,'来源：六下（余姚）/余姚市-第16课《认识智能控制系统》/余姚市-第16课认识智能控制系统-学习单网站-教师课件/assets/video-carry.mp4','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026dbd87d88530850e49b019772ebc04460'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'04875da6aab1faeea9b27a061b96b82d3243636fa4621db8fe9ee55bce654f1a','VIDEO','video-drive','video-drive.mp4','04/04875da6aab1faeea9b27a061b96b82d3243636fa4621db8fe9ee55bce654f1a.mp4','04/04875da6aab1faeea9b27a061b96b82d3243636fa4621db8fe9ee55bce654f1a.mp4',NULL,NULL,0,13331163,'来源：六下（余姚）/余姚市-第16课《认识智能控制系统》/余姚市-第16课认识智能控制系统-学习单网站-教师课件/assets/video-drive.mp4','',2
FROM biz_textbook_case c WHERE c.source_key='nb2026dbd87d88530850e49b019772ebc04460'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-bc38af70967f5c6859dee647db5c9aa62fbebb26c9611ab0aa18b1801faf2376','MATERIAL','认识智能控制系统-余姚市-第16课《认识智能控制系统》','第16课-认识智能控制系统-姚静-余姚市-第16课《认识智能控制系统》.zip','bc/bc38af70967f5c6859dee647db5c9aa62fbebb26c9611ab0aa18b1801faf2376.zip',NULL,NULL,NULL,0,34401056,'共 11 个文件，来源目录：六下（余姚）/余姚市-第16课认识智能控制系统-学习单网站-教师课件','解压后用浏览器打开入口 HTML；若页面依赖本地服务或模型文件，请按包内 README 启动。',3
FROM biz_textbook_case c WHERE c.source_key='nb2026dbd87d88530850e49b019772ebc04460'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026df658068cb9366c7e52af89aba9adef3',b.book_id,'lesson-16','认识智能控制系统','学生版','','NINGBO2026','PUBLISHED',102
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-16' AND s.kind='LESSON'
WHERE b.book_code='xxkj-6-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-be074a7adc2d31871a056df8dda6b704928dbe5863226d7b6a741ef3b9cf407d','MATERIAL','认识智能控制系统-余姚市-第16课《认识智能控制系统》','第16课-认识智能控制系统-学生版-余姚市-第16课《认识智能控制系统》.zip','be/be074a7adc2d31871a056df8dda6b704928dbe5863226d7b6a741ef3b9cf407d.zip',NULL,NULL,NULL,0,10453452,'共 1 个文件，来源目录：六下（余姚）/余姚市-第16课认识智能控制系统-学习单网站-学生版','解压后用浏览器打开入口 HTML；若页面依赖本地服务或模型文件，请按包内 README 启动。',1
FROM biz_textbook_case c WHERE c.source_key='nb2026df658068cb9366c7e52af89aba9adef3'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-dc763795d56bf425ea3f28f39c7331041877f34f1cd7ca32b0beba7e4460e8f8','MATERIAL','认识智能体-余姚市-认识智能体-余姚市梨洲小学-李炯荣(1)','第18课-认识智能体-李炯荣(1)-余姚市-认识智能体-余姚市梨洲小学-李炯荣(1).zip','dc/dc763795d56bf425ea3f28f39c7331041877f34f1cd7ca32b0beba7e4460e8f8.zip',NULL,NULL,NULL,0,7992937,'共 29 个文件，来源目录：六下（余姚）/余姚市-认识智能体-余姚市梨洲小学-李炯荣(1)','解压后用浏览器打开入口 HTML；若页面依赖本地服务或模型文件，请按包内 README 启动。\n含 Windows 程序或脚本，解压后按包内说明运行；请先确认来源可信。',1
FROM biz_textbook_case c WHERE c.source_key='b28fe6ce0d5bd19002aec26a067897ac'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb202672e9c6524e35fb121912cee2d4465697',b.book_id,'lesson-1','计算机网络','杨雨婷、胡红波','溪口中学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-1' AND s.kind='LESSON'
WHERE b.book_code='xxkj-7-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'023f131751bd43072823036a2c590625160d1e12a58cfa71012635e42bab3844','DESIGN','第1课《计算机网络》设计案例-杨雨婷、胡红波','第1课《计算机网络》设计案例-杨雨婷、胡红波.docx','02/023f131751bd43072823036a2c590625160d1e12a58cfa71012635e42bab3844.docx','previews/023f131751bd43072823036a2c590625160d1e12a58cfa71012635e42bab3844.pdf',NULL,NULL,0,496997,'来源：七上（奉化）/第1课《计算机网络》设计案例-杨雨婷、胡红波.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb202672e9c6524e35fb121912cee2d4465697'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb20268f698faee5aeac30381e1be7731fa2c1',b.book_id,'lesson-2','演进中的中国互联网','石一威','宁波市奉化区裘村中学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-2' AND s.kind='LESSON'
WHERE b.book_code='xxkj-7-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'1ada3f9e0bfc24c9d6031aca8d0a6d3ecb32b1a67a467439815c62a4f081f8ea','DESIGN','第2课《演进中的中国互联网》设计案例——石一威','第2课《演进中的中国互联网》设计案例——石一威.docx','1a/1ada3f9e0bfc24c9d6031aca8d0a6d3ecb32b1a67a467439815c62a4f081f8ea.docx','previews/1ada3f9e0bfc24c9d6031aca8d0a6d3ecb32b1a67a467439815c62a4f081f8ea.pdf',NULL,NULL,0,85073,'来源：七上（奉化）/第2课《演进中的中国互联网》设计案例——石一威.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb20268f698faee5aeac30381e1be7731fa2c1'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026cde5b7b428f2dc8e3fbd9829443639ca',b.book_id,'lesson-3','互联网应用体验','徐凯王炳科','','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-3' AND s.kind='LESSON'
WHERE b.book_code='xxkj-7-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'08373e20e1d97581e0ac90ced00607254fa4d88155577eadfc2d7fe3ace006c2','DESIGN','第3课《互联网应用体验》——徐凯王炳科','第3课《互联网应用体验》——徐凯王炳科.docx','08/08373e20e1d97581e0ac90ced00607254fa4d88155577eadfc2d7fe3ace006c2.docx','previews/08373e20e1d97581e0ac90ced00607254fa4d88155577eadfc2d7fe3ace006c2.pdf',NULL,NULL,0,24435,'来源：七上（奉化）/第3课《互联网应用体验》——徐凯王炳科.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026cde5b7b428f2dc8e3fbd9829443639ca'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026d51e06561650f4c0cea1f37b821a62b1',b.book_id,'lesson-4','互联网创新发展','汪满芬','尚田中学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-4' AND s.kind='LESSON'
WHERE b.book_code='xxkj-7-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'aa31e22f2d7e7778cd302ea3a58b48ac14b6295041af6049bc6a7ec6d8b9662b','DESIGN','第4课《互联网创新发展》教学设计（汪满芬）','第4课《互联网创新发展》教学设计（汪满芬）.docx','aa/aa31e22f2d7e7778cd302ea3a58b48ac14b6295041af6049bc6a7ec6d8b9662b.docx','previews/aa31e22f2d7e7778cd302ea3a58b48ac14b6295041af6049bc6a7ec6d8b9662b.pdf',NULL,NULL,0,22310,'来源：七上（奉化）/第4课《互联网创新发展》教学设计（汪满芬）.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026d51e06561650f4c0cea1f37b821a62b1'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-dee47407ab07cca90fa17e220dc3aac28e49fcd7845b0fab976075c2095c885e','MATERIAL','互联网与人工智能-第5课《互联网与人工智能》-王纪宇','第5课-互联网与人工智能-王纪宇-第5课《互联网与人工智能》-王纪宇.zip','de/dee47407ab07cca90fa17e220dc3aac28e49fcd7845b0fab976075c2095c885e.zip',NULL,NULL,NULL,0,35283,'共 5 个文件，来源目录：七上（奉化）/第5课《互联网与人工智能》-王纪宇','解压后用浏览器打开入口 HTML；若页面依赖本地服务或模型文件，请按包内 README 启动。',1
FROM biz_textbook_case c WHERE c.source_key='b4f0009bbd644a9fb6e7aff371aba8d7'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-267ccd3a67848be540586bc5b8cd7782714244761b75216291c0fd0cc4fd88f7','MATERIAL','互联网接入-江北区-第6课《互联网接入》','第6课-互联网接入-周旺琼-江北区-第6课《互联网接入》.zip','26/267ccd3a67848be540586bc5b8cd7782714244761b75216291c0fd0cc4fd88f7.zip',NULL,NULL,NULL,0,16822885,'共 12 个文件，来源目录：七上（奉化）/江北区-第6课《互联网接入》','解压后用浏览器打开入口 HTML；若页面依赖本地服务或模型文件，请按包内 README 启动。',1
FROM biz_textbook_case c WHERE c.source_key='f9f54e2cca91102a4c10da80968fce0b'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb202660c235a8c2215abf936e80c6575b7281',b.book_id,'lesson-6','互联网接入','杨双梅 华健','','NINGBO2026','PUBLISHED',102
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-6' AND s.kind='LESSON'
WHERE b.book_code='xxkj-7-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'577cdec22e29b7b33f52cf96dbd16b40c6e1a32d65dac818f1f237e7d499f27a','DESIGN','第6课《互联网接入》教学设计-杨双梅 华健','第6课《互联网接入》教学设计-杨双梅 华健.docx','57/577cdec22e29b7b33f52cf96dbd16b40c6e1a32d65dac818f1f237e7d499f27a.docx','previews/577cdec22e29b7b33f52cf96dbd16b40c6e1a32d65dac818f1f237e7d499f27a.pdf',NULL,NULL,0,23504,'来源：七上（奉化）/第6课《互联网接入》教学设计-杨双梅 华健.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb202660c235a8c2215abf936e80c6575b7281'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb20265526c83cab48ac04b1bfd2e512486a13',b.book_id,'lesson-7','网络服务','朱建波','宁波市奉化区锦溪书院','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-7' AND s.kind='LESSON'
WHERE b.book_code='xxkj-7-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'38af8263339628b991bbc188d67fa4622a4c13b0350fd135e30568c2c3c4c6f9','DESIGN','第7课《网络服务》——朱建波','第7课《网络服务》——朱建波.docx','38/38af8263339628b991bbc188d67fa4622a4c13b0350fd135e30568c2c3c4c6f9.docx','previews/38af8263339628b991bbc188d67fa4622a4c13b0350fd135e30568c2c3c4c6f9.pdf',NULL,NULL,0,41881,'来源：七上（奉化）/第7课《网络服务》——朱建波.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb20265526c83cab48ac04b1bfd2e512486a13'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb20268ddbd2579dbc500f13654245486057d5',b.book_id,'lesson-8','互联网应用协议','李珊君、胡凡筱','奉化区实验中学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-8' AND s.kind='LESSON'
WHERE b.book_code='xxkj-7-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'9ceb6d65dba326e29fa5eb848fbefd53d41d5b4c8afa2c2f56cb536608f8a0cf','DESIGN','第8课《互联网应用协议》——李珊君、胡凡筱','第8课《互联网应用协议》——李珊君、胡凡筱.docx','9c/9ceb6d65dba326e29fa5eb848fbefd53d41d5b4c8afa2c2f56cb536608f8a0cf.docx','previews/9ceb6d65dba326e29fa5eb848fbefd53d41d5b4c8afa2c2f56cb536608f8a0cf.pdf',NULL,NULL,0,994260,'来源：七上（奉化）/第8课《互联网应用协议》——李珊君、胡凡筱.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb20268ddbd2579dbc500f13654245486057d5'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026a14e59d485566c05ec21f8a9fa869302',b.book_id,'lesson-9','认识TCP/IP','王珊波','宁波市奉化区实验中学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-9' AND s.kind='LESSON'
WHERE b.book_code='xxkj-7-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'0aede7d4b270666d5aa89873dd8c2b39ab09e91d273f761b90c9ba51fcc6c764','DESIGN','第9课  《认识TCP_IP》—王珊波','第9课  《认识TCP_IP》—王珊波.docx','0a/0aede7d4b270666d5aa89873dd8c2b39ab09e91d273f761b90c9ba51fcc6c764.docx','previews/0aede7d4b270666d5aa89873dd8c2b39ab09e91d273f761b90c9ba51fcc6c764.pdf',NULL,NULL,0,1394338,'来源：七上（奉化）/第9课  《认识TCP_IP》—王珊波.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026a14e59d485566c05ec21f8a9fa869302'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb20261734e0a32b3b7005c006ee79de933a0a',b.book_id,'lesson-10','IP地址与DHCP','毛群娜','西坞中学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-10' AND s.kind='LESSON'
WHERE b.book_code='xxkj-7-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'af8582124932b139f22e72dc2daf24461bc5971d7229b08acf99d26f0d7c672d','WORKSHEET','第10课《ip地址和dhcp》教学案例 (毛群娜）','第10课《ip地址和dhcp》教学案例 (毛群娜）.docx','af/af8582124932b139f22e72dc2daf24461bc5971d7229b08acf99d26f0d7c672d.docx','previews/af8582124932b139f22e72dc2daf24461bc5971d7229b08acf99d26f0d7c672d.pdf',NULL,NULL,0,54657,'来源：七上（奉化）/第10课《ip地址和dhcp》教学案例 (毛群娜）.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb20261734e0a32b3b7005c006ee79de933a0a'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb202685e29a2a78f61a68adad2cafb21d2bef',b.book_id,'lesson-11','小型网络的搭建','周颖斐','西坞中学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-11' AND s.kind='LESSON'
WHERE b.book_code='xxkj-7-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'969fc63448b1aa25eb08f3b33ae5554e5138e601a3703aa1078214f16b1d32f5','DESIGN','第11课《小型网络的搭建》——周颖斐','第11课《小型网络的搭建》——周颖斐.docx','96/969fc63448b1aa25eb08f3b33ae5554e5138e601a3703aa1078214f16b1d32f5.docx','previews/969fc63448b1aa25eb08f3b33ae5554e5138e601a3703aa1078214f16b1d32f5.pdf',NULL,NULL,0,181836,'来源：七上（奉化）/第11课《小型网络的搭建》——周颖斐.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb202685e29a2a78f61a68adad2cafb21d2bef'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026700d7e8558350287788e234d15444701',b.book_id,'lesson-12','获取互联网资源','张盈瑕','奉化实验中学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-12' AND s.kind='LESSON'
WHERE b.book_code='xxkj-7-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'8b6198b945994b92e964ea64ff9e7e0a65bafab7ea34d08e91f75e9736cde450','DESIGN','第12课获取互联网资源——《制作杭州亚运会宣传册》设计案例——张盈瑕','第12课获取互联网资源——《制作杭州亚运会宣传册》设计案例——张盈瑕.docx','8b/8b6198b945994b92e964ea64ff9e7e0a65bafab7ea34d08e91f75e9736cde450.docx','previews/8b6198b945994b92e964ea64ff9e7e0a65bafab7ea34d08e91f75e9736cde450.pdf',NULL,NULL,0,115546,'来源：七上（奉化）/第12课获取互联网资源——《制作杭州亚运会宣传册》设计案例——张盈瑕.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026700d7e8558350287788e234d15444701'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026810ff7dc99194ae7ce94d0e0d1798753',b.book_id,'lesson-13','云计算','俞佳敏、马玥','宁波市奉化区锦溪书院','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-13' AND s.kind='LESSON'
WHERE b.book_code='xxkj-7-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'fa99cf08c72e5ff974c9b920decbd1077c9f5e8e5ab087c5e187318debb93b92','DESIGN','第13课《云计算》设计案例——俞佳敏、马玥','第13课《云计算》设计案例——俞佳敏、马玥.docx','fa/fa99cf08c72e5ff974c9b920decbd1077c9f5e8e5ab087c5e187318debb93b92.docx','previews/fa99cf08c72e5ff974c9b920decbd1077c9f5e8e5ab087c5e187318debb93b92.pdf',NULL,NULL,0,70067,'来源：七上（奉化）/第13课《云计算》设计案例——俞佳敏、马玥.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026810ff7dc99194ae7ce94d0e0d1798753'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026f687ab84c8c997ddd3a11cc125ade93e',b.book_id,'lesson-14','云存储','宋琪琦','宁波市奉化区萧王庙中学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-14' AND s.kind='LESSON'
WHERE b.book_code='xxkj-7-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'5a0e48872f75d236a9febc75becefe52e82789c42fd006b4fbfbad08bbb07dd0','DESIGN','第14课 《云存储》教学设计（宋琪琦）','第14课 《云存储》教学设计（宋琪琦）.docx','5a/5a0e48872f75d236a9febc75becefe52e82789c42fd006b4fbfbad08bbb07dd0.docx','previews/5a0e48872f75d236a9febc75becefe52e82789c42fd006b4fbfbad08bbb07dd0.pdf',NULL,NULL,0,437534,'来源：七上（奉化）/第14课 《云存储》教学设计（宋琪琦）.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026f687ab84c8c997ddd3a11cc125ade93e'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb20266d54e22f236ff7851d35ae1ee7314f85',b.book_id,'lesson-15','向世界介绍我的学校——规划与探究','规划与探究','向世界介绍我的学校','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-15' AND s.kind='LESSON'
WHERE b.book_code='xxkj-7-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'49ac360a5fbf00980d5e1a1a47116a521bc40ea0418f733e84cb701145a0c712','DESIGN','第15课《向世界介绍和的学校——规划与探究》设计案列-马小辉','第15课《向世界介绍和的学校——规划与探究》设计案列-马小辉.docx','49/49ac360a5fbf00980d5e1a1a47116a521bc40ea0418f733e84cb701145a0c712.docx','previews/49ac360a5fbf00980d5e1a1a47116a521bc40ea0418f733e84cb701145a0c712.pdf',NULL,NULL,0,24933,'来源：七上（奉化）/第15课《向世界介绍和的学校——规划与探究》设计案列-马小辉.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb20266d54e22f236ff7851d35ae1ee7314f85'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026243d461472bcb62110b7630dd50e301c',b.book_id,'lesson-16','向世界介绍我的学校——创作与发布','王静','','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-16' AND s.kind='LESSON'
WHERE b.book_code='xxkj-7-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'41a80300d1d34abdde5b16861f875cc5433c561c7c39510fc6fb659b5ac99b82','DESIGN','第16课《向世界介绍我的学校》设计案例—王静','第16课《向世界介绍我的学校》设计案例—王静.docx','41/41a80300d1d34abdde5b16861f875cc5433c561c7c39510fc6fb659b5ac99b82.docx','previews/41a80300d1d34abdde5b16861f875cc5433c561c7c39510fc6fb659b5ac99b82.pdf',NULL,NULL,0,26354,'来源：七上（奉化）/第16课《向世界介绍我的学校》设计案例—王静.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026243d461472bcb62110b7630dd50e301c'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-1af9e37afd559bb44edbd0d2104b21fd948259746ee7007e4872a44358c64e33','MATERIAL','人工智能的发展-江北区-第17课《人工智能的发展》','第17课-人工智能的发展-张莹洁-江北区-第17课《人工智能的发展》.zip','1a/1af9e37afd559bb44edbd0d2104b21fd948259746ee7007e4872a44358c64e33.zip',NULL,NULL,NULL,0,23693317,'共 51 个文件，来源目录：七上（奉化）/江北区-第17课《人工智能的发展》','解压后用浏览器打开入口 HTML；若页面依赖本地服务或模型文件，请按包内 README 启动。',1
FROM biz_textbook_case c WHERE c.source_key='f10a1ae14f878a781a9c0a29c9ac6bc0'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-68fc5cf73042f1c6f878a5c466456d7ef41640efa245f481fcf6a910cdada7a6','MATERIAL','生成式人工智能-江北区-第18课《生成式人工智能》','第18课-生成式人工智能-江北区展示课例（未署名）-江北区-第18课《生成式人工智能》.zip','68/68fc5cf73042f1c6f878a5c466456d7ef41640efa245f481fcf6a910cdada7a6.zip',NULL,NULL,NULL,0,74516594,'共 1 个文件，来源目录：七上（奉化）/江北区-第18课《生成式人工智能》','解压后按文件名和包内 README 使用。',1
FROM biz_textbook_case c WHERE c.source_key='58ad1cdda4a8b840c6f95196960e79d0'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-8d45d1a3eaadadfbef026975e0dbeacb9140c69362ec018a606b59dc9f102b3c','MATERIAL','智能体的搭建与应用-江北区-第19课《智能体的搭建与应用》','第19课-智能体的搭建与应用-史习阳-江北区-第19课《智能体的搭建与应用》.zip','8d/8d45d1a3eaadadfbef026975e0dbeacb9140c69362ec018a606b59dc9f102b3c.zip',NULL,NULL,NULL,0,57532675,'共 128 个文件，来源目录：七上（奉化）/江北区-第19课《智能体的搭建与应用》','需 Node.js 环境：解压后在项目目录执行 npm install，再按 README 或 package.json 启动。\n解压后用浏览器打开入口 HTML；若页面依赖本地服务或模型文件，请按包内 README 启动。',1
FROM biz_textbook_case c WHERE c.source_key='e030a29d1210eeacb137ee49572daedc'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-9f8b5a1ab876d060d4ee861f5da2da6ffc2c45c6afe3e41c0ab2b9bfc285cc80','MATERIAL','人工智能的影响-江北区-第20课《人工智能的影响》','第20课-人工智能的影响-维度-江北区-第20课《人工智能的影响》.zip','9f/9f8b5a1ab876d060d4ee861f5da2da6ffc2c45c6afe3e41c0ab2b9bfc285cc80.zip',NULL,NULL,NULL,0,68361,'共 5 个文件，来源目录：七上（奉化）/江北区-第20课《人工智能的影响》','解压后用浏览器打开入口 HTML；若页面依赖本地服务或模型文件，请按包内 README 启动。',1
FROM biz_textbook_case c WHERE c.source_key='7b0175147ac66f43b6bf614b3a4f67f4'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb202664cce672c229079ab52a2d12e68bb4a5',b.book_id,'lesson-1','互联网和物联网','王蓓娜','姜山镇中心初级中学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-1' AND s.kind='LESSON'
WHERE b.book_code='xxkj-7-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'d63eefc8a4868cd583a7dbecd5d53ff3b7feb9ef779a356d7c7a8fbeaa44ff75','DESIGN','互联网与物联网【教学设计】','互联网与物联网【教学设计】.doc','d6/d63eefc8a4868cd583a7dbecd5d53ff3b7feb9ef779a356d7c7a8fbeaa44ff75.doc','previews/d63eefc8a4868cd583a7dbecd5d53ff3b7feb9ef779a356d7c7a8fbeaa44ff75.pdf',NULL,NULL,0,39424,'来源：七下（鄞州）/第1课互联网和物联网/互联网与物联网【教学设计】.doc','',1
FROM biz_textbook_case c WHERE c.source_key='nb202664cce672c229079ab52a2d12e68bb4a5'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'082903d69f1ba2c99408409575704b29b00e9b6038a8b8f965ab39fcf17f7ab4','SLIDES','互联网与物联网','互联网与物联网.pptx','08/082903d69f1ba2c99408409575704b29b00e9b6038a8b8f965ab39fcf17f7ab4.pptx','previews/082903d69f1ba2c99408409575704b29b00e9b6038a8b8f965ab39fcf17f7ab4.pdf',NULL,NULL,0,168595182,'来源：七下（鄞州）/第1课互联网和物联网/互联网与物联网.pptx','',2
FROM biz_textbook_case c WHERE c.source_key='nb202664cce672c229079ab52a2d12e68bb4a5'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'8a09520c6a5701ae3972a6cc007f7f721694d479753356167f9fcd688e409003','VIDEO','从互联到物联','从互联到物联.mp4','8a/8a09520c6a5701ae3972a6cc007f7f721694d479753356167f9fcd688e409003.mp4','8a/8a09520c6a5701ae3972a6cc007f7f721694d479753356167f9fcd688e409003.mp4',NULL,NULL,0,14518732,'来源：七下（鄞州）/第1课互联网和物联网/从互联到物联.mp4','',3
FROM biz_textbook_case c WHERE c.source_key='nb202664cce672c229079ab52a2d12e68bb4a5'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'4d27c7fa809dc26faaae7badb5762dc53f0c47a2d978020ed4a4b3b25c35d62e','VIDEO','物联网应用','物联网应用.mp4','4d/4d27c7fa809dc26faaae7badb5762dc53f0c47a2d978020ed4a4b3b25c35d62e.mp4','4d/4d27c7fa809dc26faaae7badb5762dc53f0c47a2d978020ed4a4b3b25c35d62e.mp4',NULL,NULL,0,73227860,'来源：七下（鄞州）/第1课互联网和物联网/物联网应用.mp4','',4
FROM biz_textbook_case c WHERE c.source_key='nb202664cce672c229079ab52a2d12e68bb4a5'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'c17dbd3dfdeecb0b4fafa6dcbdea392d0d31c14e394c606de88f8fd294fb3929','VIDEO','物联网起源','物联网起源.mp4','c1/c17dbd3dfdeecb0b4fafa6dcbdea392d0d31c14e394c606de88f8fd294fb3929.mp4','c1/c17dbd3dfdeecb0b4fafa6dcbdea392d0d31c14e394c606de88f8fd294fb3929.mp4',NULL,NULL,0,79976036,'来源：七下（鄞州）/第1课互联网和物联网/物联网起源.mp4','',5
FROM biz_textbook_case c WHERE c.source_key='nb202664cce672c229079ab52a2d12e68bb4a5'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'40abc7d1ae8d7d9ecc82bf47124fa82d12aa49560b7889a07ba66920e70fa158','MATERIAL','物联网重要事件','物联网重要事件.doc','40/40abc7d1ae8d7d9ecc82bf47124fa82d12aa49560b7889a07ba66920e70fa158.doc','previews/40abc7d1ae8d7d9ecc82bf47124fa82d12aa49560b7889a07ba66920e70fa158.pdf',NULL,NULL,0,14848,'来源：七下（鄞州）/第1课互联网和物联网/物联网重要事件.doc','',6
FROM biz_textbook_case c WHERE c.source_key='nb202664cce672c229079ab52a2d12e68bb4a5'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-92626ec4351dc65560fdb3eee1599380bfcde5e61845d15332bf8f756635848f','MATERIAL','互联网和物联网-第1课互联网和物联网','第1课-互联网和物联网-王蓓娜-第1课互联网和物联网.zip','92/92626ec4351dc65560fdb3eee1599380bfcde5e61845d15332bf8f756635848f.zip',NULL,NULL,NULL,0,698,'共 1 个文件，来源目录：七下（鄞州）/第1课互联网和物联网','解压后按文件名和包内 README 使用。',7
FROM biz_textbook_case c WHERE c.source_key='nb202664cce672c229079ab52a2d12e68bb4a5'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026009b29856afed239d41157b15a49465e',b.book_id,'lesson-2','走进物联网','施晓军','鄞州区邱隘董玉娣实验中学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-2' AND s.kind='LESSON'
WHERE b.book_code='xxkj-7-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'fb5e719948254124877f6c4f42dcd3f2c8d00f2b7e79b34ea12307a8fad06cec','DESIGN','第2课走进物联网','第2课走进物联网.docx','fb/fb5e719948254124877f6c4f42dcd3f2c8d00f2b7e79b34ea12307a8fad06cec.docx','previews/fb5e719948254124877f6c4f42dcd3f2c8d00f2b7e79b34ea12307a8fad06cec.pdf',NULL,NULL,0,94405,'来源：七下（鄞州）/第2课走进物联网.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026009b29856afed239d41157b15a49465e'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026f6764bf5ea567d690698d3643a4006cc',b.book_id,'lesson-3','物联网与生活','周永来','宁波市鄞州区云龙镇中学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-3' AND s.kind='LESSON'
WHERE b.book_code='xxkj-7-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'bc2505d0e633662f07b76d4e3b2bedaef39bb512ed3c73437463dc7fdb6ba8ca','DESIGN','浙教版（2023） 第3课 物联网与生活','浙教版（2023） 第3课 物联网与生活.docx','bc/bc2505d0e633662f07b76d4e3b2bedaef39bb512ed3c73437463dc7fdb6ba8ca.docx','previews/bc2505d0e633662f07b76d4e3b2bedaef39bb512ed3c73437463dc7fdb6ba8ca.pdf',NULL,NULL,0,426772,'来源：七下（鄞州）/第3课物联网与生活/浙教版（2023） 第3课 物联网与生活.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026f6764bf5ea567d690698d3643a4006cc'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'ac29c5d38cda79edcf6d38c47a618c4ca57fa0a5fdc0c32483b1dffd69de01ec','VIDEO','智能交通物联网的应用','智能交通物联网的应用.mp4','ac/ac29c5d38cda79edcf6d38c47a618c4ca57fa0a5fdc0c32483b1dffd69de01ec.mp4','ac/ac29c5d38cda79edcf6d38c47a618c4ca57fa0a5fdc0c32483b1dffd69de01ec.mp4',NULL,NULL,0,36646213,'来源：七下（鄞州）/第3课物联网与生活/物联网与生活的视频/智能交通物联网的应用.mp4','',2
FROM biz_textbook_case c WHERE c.source_key='nb2026f6764bf5ea567d690698d3643a4006cc'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'9b800428b0ed0d613847a537217250018f1bb0f5f1456d6d311612a6acd6d361','VIDEO','智能家居物联网的应用','智能家居物联网的应用.mp4','9b/9b800428b0ed0d613847a537217250018f1bb0f5f1456d6d311612a6acd6d361.mp4','9b/9b800428b0ed0d613847a537217250018f1bb0f5f1456d6d311612a6acd6d361.mp4',NULL,NULL,0,37009802,'来源：七下（鄞州）/第3课物联网与生活/物联网与生活的视频/智能家居物联网的应用.mp4','',3
FROM biz_textbook_case c WHERE c.source_key='nb2026f6764bf5ea567d690698d3643a4006cc'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'e5a95940854127c4054eabc010f1889544a054a94c0783218b5bc561b83bdce3','VIDEO','智能物流物联网的应用','智能物流物联网的应用.mp4','e5/e5a95940854127c4054eabc010f1889544a054a94c0783218b5bc561b83bdce3.mp4','e5/e5a95940854127c4054eabc010f1889544a054a94c0783218b5bc561b83bdce3.mp4',NULL,NULL,0,37927467,'来源：七下（鄞州）/第3课物联网与生活/物联网与生活的视频/智能物流物联网的应用.mp4','',4
FROM biz_textbook_case c WHERE c.source_key='nb2026f6764bf5ea567d690698d3643a4006cc'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'6e3c0b220f115ff2f251c79161af3282cbb876038797a6dc727dca63f8b69b56','SLIDES','第3课  物联网与生活','第3课  物联网与生活.pptx','6e/6e3c0b220f115ff2f251c79161af3282cbb876038797a6dc727dca63f8b69b56.pptx','previews/6e3c0b220f115ff2f251c79161af3282cbb876038797a6dc727dca63f8b69b56.pdf',NULL,NULL,0,5277713,'来源：七下（鄞州）/第3课物联网与生活/第3课  物联网与生活.pptx','',5
FROM biz_textbook_case c WHERE c.source_key='nb2026f6764bf5ea567d690698d3643a4006cc'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb202692f9bc5cb42fa3c2a3c6839c4a634d55',b.book_id,'lesson-4','物联网展望','方艳威','宁波市第七中学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-4' AND s.kind='LESSON'
WHERE b.book_code='xxkj-7-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'87559f4aed6fecc5e1aa08d1d9ab9632648a5fe65c915144add62d4fd54a7bfc','DESIGN','第四课《物联网展望》教案','第四课《物联网展望》教案.docx','87/87559f4aed6fecc5e1aa08d1d9ab9632648a5fe65c915144add62d4fd54a7bfc.docx','previews/87559f4aed6fecc5e1aa08d1d9ab9632648a5fe65c915144add62d4fd54a7bfc.pdf',NULL,NULL,0,62126,'来源：七下（鄞州）/第4课物联网展望/第四课《物联网展望》教案.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb202692f9bc5cb42fa3c2a3c6839c4a634d55'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'1195109b4c6e60095b8fd42afbaffcb50aa5bae52b14182f9517cb910521141b','VIDEO','中国物联网','中国物联网.mp4','11/1195109b4c6e60095b8fd42afbaffcb50aa5bae52b14182f9517cb910521141b.mp4','11/1195109b4c6e60095b8fd42afbaffcb50aa5bae52b14182f9517cb910521141b.mp4',NULL,NULL,0,3430610,'来源：七下（鄞州）/第4课物联网展望/中国物联网.mp4','',2
FROM biz_textbook_case c WHERE c.source_key='nb202692f9bc5cb42fa3c2a3c6839c4a634d55'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'ac3f89e75d0d8f9c462614554320c109ee05d8cef7cd39fbf1c634ee3d7318cc','VIDEO','大数据特点','大数据特点.mp4','ac/ac3f89e75d0d8f9c462614554320c109ee05d8cef7cd39fbf1c634ee3d7318cc.mp4','ac/ac3f89e75d0d8f9c462614554320c109ee05d8cef7cd39fbf1c634ee3d7318cc.mp4',NULL,NULL,0,9209568,'来源：七下（鄞州）/第4课物联网展望/大数据特点.mp4','',3
FROM biz_textbook_case c WHERE c.source_key='nb202692f9bc5cb42fa3c2a3c6839c4a634d55'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'e538d9318014a2478186eb795f4c5ca111323e91b70e7da9df58ff656ff4b19d','WORKSHEET','学生任务单','学生任务单.pptx','e5/e538d9318014a2478186eb795f4c5ca111323e91b70e7da9df58ff656ff4b19d.pptx','previews/e538d9318014a2478186eb795f4c5ca111323e91b70e7da9df58ff656ff4b19d.pdf',NULL,NULL,0,66234,'来源：七下（鄞州）/第4课物联网展望/学生任务单.pptx','',4
FROM biz_textbook_case c WHERE c.source_key='nb202692f9bc5cb42fa3c2a3c6839c4a634d55'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'5d0c267b67a76b08013bb255fb65b1ceade78ae4ff2dc2b744f3adabb8cb4d39','VIDEO','未来生活畅想','未来生活畅想.mp4','5d/5d0c267b67a76b08013bb255fb65b1ceade78ae4ff2dc2b744f3adabb8cb4d39.mp4','5d/5d0c267b67a76b08013bb255fb65b1ceade78ae4ff2dc2b744f3adabb8cb4d39.mp4',NULL,NULL,0,10618797,'来源：七下（鄞州）/第4课物联网展望/未来生活畅想.mp4','',5
FROM biz_textbook_case c WHERE c.source_key='nb202692f9bc5cb42fa3c2a3c6839c4a634d55'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'f74ab2fae392000466f4eada843ef5ba3f46c1a863d6837f4ad4543ef349beb5','VIDEO','物联网与大数据','物联网与大数据.mp4','f7/f74ab2fae392000466f4eada843ef5ba3f46c1a863d6837f4ad4543ef349beb5.mp4','f7/f74ab2fae392000466f4eada843ef5ba3f46c1a863d6837f4ad4543ef349beb5.mp4',NULL,NULL,0,5504784,'来源：七下（鄞州）/第4课物联网展望/物联网与大数据.mp4','',6
FROM biz_textbook_case c WHERE c.source_key='nb202692f9bc5cb42fa3c2a3c6839c4a634d55'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'3c127ab1a54df53585595e89b6ed2226e6e7cd09027b2bbe037dc9564266d145','SLIDES','第四课《物联网展望》','第四课《物联网展望》.pptx','3c/3c127ab1a54df53585595e89b6ed2226e6e7cd09027b2bbe037dc9564266d145.pptx','previews/3c127ab1a54df53585595e89b6ed2226e6e7cd09027b2bbe037dc9564266d145.pdf',NULL,NULL,0,339090,'来源：七下（鄞州）/第4课物联网展望/第四课《物联网展望》.pptx','',7
FROM biz_textbook_case c WHERE c.source_key='nb202692f9bc5cb42fa3c2a3c6839c4a634d55'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-199597e9dce631714c7afbc8802b6c382db0d51cce67526715eb08ba6168ee6a','MATERIAL','人工智能物联网-象山县-第5课《人工智能物联网》','第5课-人工智能物联网-郑东旭-象山县-第5课《人工智能物联网》.zip','19/199597e9dce631714c7afbc8802b6c382db0d51cce67526715eb08ba6168ee6a.zip',NULL,NULL,NULL,0,1856660,'共 4 个文件，来源目录：七下（鄞州）/象山县-第5课《人工智能物联网》','解压后用浏览器打开入口 HTML；若页面依赖本地服务或模型文件，请按包内 README 启动。',1
FROM biz_textbook_case c WHERE c.source_key='4d838a023f779bca751d0ff3b68d6845'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb20268e0f260939311ac7aa3791254331d137',b.book_id,'lesson-6','感知与识别','陈学渊','宁波市鄞州区东吴镇中学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-6' AND s.kind='LESSON'
WHERE b.book_code='xxkj-7-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'5a6a66a926717e6fd050cf5f23133fec69d74562692405f0ced9d0e4dd784249','DESIGN','第6课 《感知与识别》 教学设计','第6课 《感知与识别》 教学设计.docx','5a/5a6a66a926717e6fd050cf5f23133fec69d74562692405f0ced9d0e4dd784249.docx','previews/5a6a66a926717e6fd050cf5f23133fec69d74562692405f0ced9d0e4dd784249.pdf',NULL,NULL,0,44330,'来源：七下（鄞州）/第6课感知与识别/第6课 《感知与识别》 教学设计.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb20268e0f260939311ac7aa3791254331d137'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'63c94d63c532dca611c04339f2c4c41cfaa3e3edb064e00f4d622c45a2336e21','VIDEO','二维码技术','二维码技术.mp4','63/63c94d63c532dca611c04339f2c4c41cfaa3e3edb064e00f4d622c45a2336e21.mp4','63/63c94d63c532dca611c04339f2c4c41cfaa3e3edb064e00f4d622c45a2336e21.mp4',NULL,NULL,0,3777022,'来源：七下（鄞州）/第6课感知与识别/二维码技术.mp4','',2
FROM biz_textbook_case c WHERE c.source_key='nb20268e0f260939311ac7aa3791254331d137'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'6e3d08cef54e36f4a63d0e50cf9209d6d4978c961e0b3a6963b7c3005218d259','VIDEO','图书馆自助借书','图书馆自助借书.mp4','6e/6e3d08cef54e36f4a63d0e50cf9209d6d4978c961e0b3a6963b7c3005218d259.mp4','6e/6e3d08cef54e36f4a63d0e50cf9209d6d4978c961e0b3a6963b7c3005218d259.mp4',NULL,NULL,0,3931428,'来源：七下（鄞州）/第6课感知与识别/图书馆自助借书.mp4','',3
FROM biz_textbook_case c WHERE c.source_key='nb20268e0f260939311ac7aa3791254331d137'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'dac891db21d7b781b6531a8667acd2d89136900d4308d3e411b0bbd9edb49bb5','VIDEO','定位技术的种类和区别','定位技术的种类和区别.mp4','da/dac891db21d7b781b6531a8667acd2d89136900d4308d3e411b0bbd9edb49bb5.mp4','da/dac891db21d7b781b6531a8667acd2d89136900d4308d3e411b0bbd9edb49bb5.mp4',NULL,NULL,0,10116953,'来源：七下（鄞州）/第6课感知与识别/定位技术的种类和区别.mp4','',4
FROM biz_textbook_case c WHERE c.source_key='nb20268e0f260939311ac7aa3791254331d137'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'90b69b9bba35aecc0d25c85e8c37205e5a4ea69788bc995bad6ddc581932bf76','VIDEO','射频识别技术','射频识别技术.mp4','90/90b69b9bba35aecc0d25c85e8c37205e5a4ea69788bc995bad6ddc581932bf76.mp4','90/90b69b9bba35aecc0d25c85e8c37205e5a4ea69788bc995bad6ddc581932bf76.mp4',NULL,NULL,0,3068848,'来源：七下（鄞州）/第6课感知与识别/射频识别技术.mp4','',5
FROM biz_textbook_case c WHERE c.source_key='nb20268e0f260939311ac7aa3791254331d137'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'b6fb66fb79275c05db32b1b545230e8ecca34889f0b7c82412d84fbbe355dbbf','VIDEO','条形码','条形码.mp4','b6/b6fb66fb79275c05db32b1b545230e8ecca34889f0b7c82412d84fbbe355dbbf.mp4','b6/b6fb66fb79275c05db32b1b545230e8ecca34889f0b7c82412d84fbbe355dbbf.mp4',NULL,NULL,0,2022302,'来源：七下（鄞州）/第6课感知与识别/条形码.mp4','',6
FROM biz_textbook_case c WHERE c.source_key='nb20268e0f260939311ac7aa3791254331d137'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'7ce1d7ce41df0980c241b1c18e34ab1a4d34886279b939f9ba090f9eead66044','WORKSHEET','第6课 《感知与识别》 导学案','第6课 《感知与识别》 导学案.docx','7c/7ce1d7ce41df0980c241b1c18e34ab1a4d34886279b939f9ba090f9eead66044.docx','previews/7ce1d7ce41df0980c241b1c18e34ab1a4d34886279b939f9ba090f9eead66044.pdf',NULL,NULL,0,14889,'来源：七下（鄞州）/第6课感知与识别/第6课 《感知与识别》 导学案.docx','',7
FROM biz_textbook_case c WHERE c.source_key='nb20268e0f260939311ac7aa3791254331d137'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'27f80b8aa63f2e73fff9667d5407853c4406ee6f9b9c88fda013320b9babe2af','SLIDES','第6课 《感知与识别》','第6课 《感知与识别》.pptx','27/27f80b8aa63f2e73fff9667d5407853c4406ee6f9b9c88fda013320b9babe2af.pptx','previews/27f80b8aa63f2e73fff9667d5407853c4406ee6f9b9c88fda013320b9babe2af.pdf',NULL,NULL,0,20991548,'来源：七下（鄞州）/第6课感知与识别/第6课 《感知与识别》.pptx','',8
FROM biz_textbook_case c WHERE c.source_key='nb20268e0f260939311ac7aa3791254331d137'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb202689f1877fed5601b93a65c293a90846eb',b.book_id,'lesson-7','常见的传感器','孙静静','李关弟中学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-7' AND s.kind='LESSON'
WHERE b.book_code='xxkj-7-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'dd74b3f290f0f807f8888d34ebe237b792dcd48c455ac3d8a0f61d506d3333da','DESIGN','教学设计——第七课','教学设计——第七课.doc','dd/dd74b3f290f0f807f8888d34ebe237b792dcd48c455ac3d8a0f61d506d3333da.doc','previews/dd74b3f290f0f807f8888d34ebe237b792dcd48c455ac3d8a0f61d506d3333da.pdf',NULL,NULL,0,80896,'来源：七下（鄞州）/第7课常见的传感器/教学设计——第七课.doc','',1
FROM biz_textbook_case c WHERE c.source_key='nb202689f1877fed5601b93a65c293a90846eb'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'c96718874c28ee02fa77e98e31cc31832b2bb6546d2b645ca1243818c966569c','VIDEO','代客泊车','代客泊车.mp4','c9/c96718874c28ee02fa77e98e31cc31832b2bb6546d2b645ca1243818c966569c.mp4','c9/c96718874c28ee02fa77e98e31cc31832b2bb6546d2b645ca1243818c966569c.mp4',NULL,NULL,0,28193302,'来源：七下（鄞州）/第7课常见的传感器/代客泊车.mp4','',2
FROM biz_textbook_case c WHERE c.source_key='nb202689f1877fed5601b93a65c293a90846eb'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'e7d5546a5baf0cc8f76d18b80babc99ef9340c57fdaabe753043ed9319dd17cd','SLIDES','信息科技第七课','信息科技第七课.pptx','e7/e7d5546a5baf0cc8f76d18b80babc99ef9340c57fdaabe753043ed9319dd17cd.pptx','previews/e7d5546a5baf0cc8f76d18b80babc99ef9340c57fdaabe753043ed9319dd17cd.pdf',NULL,NULL,0,693789,'来源：七下（鄞州）/第7课常见的传感器/信息科技第七课.pptx','',3
FROM biz_textbook_case c WHERE c.source_key='nb202689f1877fed5601b93a65c293a90846eb'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'8352a86778b6b68a714bed5f10c8528d28042edce727e4997630b72f2ab345a9','WORKSHEET','随堂练习','随堂练习.docx','83/8352a86778b6b68a714bed5f10c8528d28042edce727e4997630b72f2ab345a9.docx','previews/8352a86778b6b68a714bed5f10c8528d28042edce727e4997630b72f2ab345a9.pdf',NULL,NULL,0,14030,'来源：七下（鄞州）/第7课常见的传感器/随堂练习.docx','',4
FROM biz_textbook_case c WHERE c.source_key='nb202689f1877fed5601b93a65c293a90846eb'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-7dac089fb37e6c00af262d59b62dc23134f4ded5dab98c879f9f43c645837ee3','MATERIAL','常见的传感器-第7课常见的传感器','第7课-常见的传感器-孙静静-第7课常见的传感器.zip','7d/7dac089fb37e6c00af262d59b62dc23134f4ded5dab98c879f9f43c645837ee3.zip',NULL,NULL,NULL,0,5659557,'共 33 个文件，来源目录：七下（鄞州）/第7课常见的传感器','解压后按文件名和包内 README 使用。',5
FROM biz_textbook_case c WHERE c.source_key='nb202689f1877fed5601b93a65c293a90846eb'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026eb554c8ecfbcda1a33c5e3bbbc324495',b.book_id,'lesson-8','传感器的应用','王晓燕','宁波市鄞州区潘火实验中学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-8' AND s.kind='LESSON'
WHERE b.book_code='xxkj-7-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'fcafc0a3f438142c1a8145e336c82498d995d020acb95474c191207ea1d293be','MATERIAL','第8课传感器的应用','第8课传感器的应用.docx','fc/fcafc0a3f438142c1a8145e336c82498d995d020acb95474c191207ea1d293be.docx','previews/fcafc0a3f438142c1a8145e336c82498d995d020acb95474c191207ea1d293be.pdf',NULL,NULL,0,120203,'来源：七下（鄞州）/第8课传感器的应用.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026eb554c8ecfbcda1a33c5e3bbbc324495'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-57f9529be7cb262e1a14c92b8282a74cda139029e6fbfa969f4916588ec3b5be','MATERIAL','传感器的应用-配套材料','第8课-传感器的应用-王晓燕-配套材料.zip','57/57f9529be7cb262e1a14c92b8282a74cda139029e6fbfa969f4916588ec3b5be.zip',NULL,NULL,NULL,0,306778,'共 1 个文件，来源目录：七下（鄞州）/第8课传感器的应用.docx','解压后按文件名和包内 README 使用。',2
FROM biz_textbook_case c WHERE c.source_key='nb2026eb554c8ecfbcda1a33c5e3bbbc324495'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb20265cbeabcb966bc7889991555daae99bec',b.book_id,'lesson-9','无线通信技术','李艳静','宁波市春晓中学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-9' AND s.kind='LESSON'
WHERE b.book_code='xxkj-7-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'f066c0a65518befe17d56deeba67e446aa0dc6f2724b1e309bb5e9b28398566c','DESIGN','浙教版第九课《无线通信技术》教学设计（宁波市春晓中学李艳静）','浙教版第九课《无线通信技术》教学设计（宁波市春晓中学李艳静）.docx','f0/f066c0a65518befe17d56deeba67e446aa0dc6f2724b1e309bb5e9b28398566c.docx','previews/f066c0a65518befe17d56deeba67e446aa0dc6f2724b1e309bb5e9b28398566c.pdf',NULL,NULL,0,202416,'来源：七下（鄞州）/第9课无线通信技术/浙教版第九课《无线通信技术》教学设计（宁波市春晓中学李艳静）.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb20265cbeabcb966bc7889991555daae99bec'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'95a2d72c4763963468ee7c82f2b2dfc47306a5d4162e541802c7772e15ff3d7d','VIDEO','手环连接视频','手环连接视频.mp4','95/95a2d72c4763963468ee7c82f2b2dfc47306a5d4162e541802c7772e15ff3d7d.mp4','95/95a2d72c4763963468ee7c82f2b2dfc47306a5d4162e541802c7772e15ff3d7d.mp4',NULL,NULL,0,12974067,'来源：七下（鄞州）/第9课无线通信技术/护航计划/手环连接视频.mp4','',2
FROM biz_textbook_case c WHERE c.source_key='nb20265cbeabcb966bc7889991555daae99bec'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'3849fca5ede6bbc37d5fcd7a06620e988aafcc93b581c085d4be1af5f4516825','MATERIAL','活动手册','活动手册.doc','38/3849fca5ede6bbc37d5fcd7a06620e988aafcc93b581c085d4be1af5f4516825.doc','previews/3849fca5ede6bbc37d5fcd7a06620e988aafcc93b581c085d4be1af5f4516825.pdf',NULL,NULL,0,4490788,'来源：七下（鄞州）/第9课无线通信技术/护航计划/活动手册.doc','',3
FROM biz_textbook_case c WHERE c.source_key='nb20265cbeabcb966bc7889991555daae99bec'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'42e9c840e5521763114fa221c84420c2a4267685a6d239aec8cd61aecd8e16ed','SLIDES','课件','课件.pptx','42/42e9c840e5521763114fa221c84420c2a4267685a6d239aec8cd61aecd8e16ed.pptx','previews/42e9c840e5521763114fa221c84420c2a4267685a6d239aec8cd61aecd8e16ed.pdf',NULL,NULL,0,1091104,'来源：七下（鄞州）/第9课无线通信技术/课件.pptx','',4
FROM biz_textbook_case c WHERE c.source_key='nb20265cbeabcb966bc7889991555daae99bec'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-d07eb2beecea31789f09885060a6b2b0f2682b2bb42b5f4b9a80e0567519bc13','MATERIAL','无线通信技术-第9课无线通信技术','第9课-无线通信技术-李艳静-第9课无线通信技术.zip','d0/d07eb2beecea31789f09885060a6b2b0f2682b2bb42b5f4b9a80e0567519bc13.zip',NULL,NULL,NULL,0,2066,'共 2 个文件，来源目录：七下（鄞州）/第9课无线通信技术','解压后按文件名和包内 README 使用。',5
FROM biz_textbook_case c WHERE c.source_key='nb20265cbeabcb966bc7889991555daae99bec'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb20261605c26e74aa0dc147e5fb7ecf794d79',b.book_id,'lesson-10','物联网协议','沈怡嘉','宁波市鄞州区首南街道中心初级中学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-10' AND s.kind='LESSON'
WHERE b.book_code='xxkj-7-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'0673335b91ccd4dc3f1d820ebcf2121ca5a0a393535027db7429f0dea5a8d5aa','DESIGN','《物联网协议》教学设计','《物联网协议》教学设计.docx','06/0673335b91ccd4dc3f1d820ebcf2121ca5a0a393535027db7429f0dea5a8d5aa.docx','previews/0673335b91ccd4dc3f1d820ebcf2121ca5a0a393535027db7429f0dea5a8d5aa.pdf',NULL,NULL,0,188572,'来源：七下（鄞州）/第10课 物联网协议/《物联网协议》教学设计.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb20261605c26e74aa0dc147e5fb7ecf794d79'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'1e3676bb6fcf7faff5425333f1bf3c1c16bd9b49bfa4a92b164a0b1e33698acc','VIDEO','MQTT与HTTP的区别','MQTT与HTTP的区别.mp4','1e/1e3676bb6fcf7faff5425333f1bf3c1c16bd9b49bfa4a92b164a0b1e33698acc.mp4','1e/1e3676bb6fcf7faff5425333f1bf3c1c16bd9b49bfa4a92b164a0b1e33698acc.mp4',NULL,NULL,0,21801837,'来源：七下（鄞州）/第10课 物联网协议/MQTT与HTTP的区别.mp4','',2
FROM biz_textbook_case c WHERE c.source_key='nb20261605c26e74aa0dc147e5fb7ecf794d79'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'d48e8783bd6a268c0e95371e00eacf9f5795c549ce7aed72b761e1e4d3b67d49','VIDEO','MQTT协议','MQTT协议.mp4','d4/d48e8783bd6a268c0e95371e00eacf9f5795c549ce7aed72b761e1e4d3b67d49.mp4','d4/d48e8783bd6a268c0e95371e00eacf9f5795c549ce7aed72b761e1e4d3b67d49.mp4',NULL,NULL,0,10666511,'来源：七下（鄞州）/第10课 物联网协议/MQTT协议.mp4','',3
FROM biz_textbook_case c WHERE c.source_key='nb20261605c26e74aa0dc147e5fb7ecf794d79'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'5be9ff3ee836bb77c1711a87a93faa0db31170bb87ae65af427394e6c0466bd9','SLIDES','《物联网协议》','《物联网协议》.pptx','5b/5be9ff3ee836bb77c1711a87a93faa0db31170bb87ae65af427394e6c0466bd9.pptx','previews/5be9ff3ee836bb77c1711a87a93faa0db31170bb87ae65af427394e6c0466bd9.pdf',NULL,NULL,0,150194726,'来源：七下（鄞州）/第10课 物联网协议/《物联网协议》.pptx','',4
FROM biz_textbook_case c WHERE c.source_key='nb20261605c26e74aa0dc147e5fb7ecf794d79'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'3cd0f4556011e997c66aeaa971db7bcfac6056ed3f7169331245e0e108158109','WORKSHEET','《物联网协议》导学单','《物联网协议》导学单.docx','3c/3cd0f4556011e997c66aeaa971db7bcfac6056ed3f7169331245e0e108158109.docx','previews/3cd0f4556011e997c66aeaa971db7bcfac6056ed3f7169331245e0e108158109.pdf',NULL,NULL,0,13372,'来源：七下（鄞州）/第10课 物联网协议/《物联网协议》导学单.docx','',5
FROM biz_textbook_case c WHERE c.source_key='nb20261605c26e74aa0dc147e5fb7ecf794d79'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'e08829e6ff14e470e50bca357508b3dd9e2b6cc07eb69c9db08c2291bc045de9','MATERIAL','一文读懂MQTT协议','一文读懂MQTT协议.docx','e0/e08829e6ff14e470e50bca357508b3dd9e2b6cc07eb69c9db08c2291bc045de9.docx','previews/e08829e6ff14e470e50bca357508b3dd9e2b6cc07eb69c9db08c2291bc045de9.pdf',NULL,NULL,0,666026,'来源：七下（鄞州）/第10课 物联网协议/一文读懂MQTT协议.docx','',6
FROM biz_textbook_case c WHERE c.source_key='nb20261605c26e74aa0dc147e5fb7ecf794d79'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb202635f295d88ca06462b4fd38028d6d467c',b.book_id,'lesson-12','物联网数据的获取','顾海燕 钱彩艳','宁波市鄞州实验中学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-12' AND s.kind='LESSON'
WHERE b.book_code='xxkj-7-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'3d7db180411d42ef9675ff05ece39f99d4cccf254943cbd85e273d4f83264ba6','MATERIAL','第11课 物联网数据的获取','第11课 物联网数据的获取.docx','3d/3d7db180411d42ef9675ff05ece39f99d4cccf254943cbd85e273d4f83264ba6.docx','previews/3d7db180411d42ef9675ff05ece39f99d4cccf254943cbd85e273d4f83264ba6.pdf',NULL,NULL,0,623237,'来源：七下（鄞州）/第11课 物联网数据的获取.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb202635f295d88ca06462b4fd38028d6d467c'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026fb31f97aed0dc255887195475e9a22bd',b.book_id,'lesson-13','物联网数据的汇集与使用','顾海燕 钱彩艳','宁波市鄞州实验中学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-13' AND s.kind='LESSON'
WHERE b.book_code='xxkj-7-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'7584c1c7238c837788a3ed34d4d05522aef96da241f6b9031f0415815b0fa36e','MATERIAL','第12课 物联网数据的汇集与使用','第12课 物联网数据的汇集与使用.docx','75/7584c1c7238c837788a3ed34d4d05522aef96da241f6b9031f0415815b0fa36e.docx','previews/7584c1c7238c837788a3ed34d4d05522aef96da241f6b9031f0415815b0fa36e.pdf',NULL,NULL,0,288452,'来源：七下（鄞州）/第12课 物联网数据的汇集与使用.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026fb31f97aed0dc255887195475e9a22bd'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026bd9fc62612c8db6720dd318764284880',b.book_id,'lesson-14','物联网控制与反馈','','','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-14' AND s.kind='LESSON'
WHERE b.book_code='xxkj-7-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'2ec7d3febe1dc9851d42b89d88709e282a37fd6492e113bdaccd717731c1761f','DESIGN','第13课物联网控制与反馈','第13课物联网控制与反馈.docx','2e/2ec7d3febe1dc9851d42b89d88709e282a37fd6492e113bdaccd717731c1761f.docx','previews/2ec7d3febe1dc9851d42b89d88709e282a37fd6492e113bdaccd717731c1761f.pdf',NULL,NULL,0,21522,'来源：七下（鄞州）/第13课物联网控制与反馈.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026bd9fc62612c8db6720dd318764284880'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026c42582e588a8ca1bf0e9b54f8fba2d76',b.book_id,'lesson-15','物联系统原型的程序编写','黄素媛','宁波市曙光中学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-15' AND s.kind='LESSON'
WHERE b.book_code='xxkj-7-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'9c49497d796e90c3e5438645c6ba9297896946d5c9c63a59b90085701201fc48','DESIGN','第14课教案','第14课教案.docx','9c/9c49497d796e90c3e5438645c6ba9297896946d5c9c63a59b90085701201fc48.docx','previews/9c49497d796e90c3e5438645c6ba9297896946d5c9c63a59b90085701201fc48.pdf',NULL,NULL,0,214063,'来源：七下（鄞州）/第14课 《物联系统原型的程序编写》/第14课教案.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026c42582e588a8ca1bf0e9b54f8fba2d76'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'ff870318f3f81a1d51cc482a5d65aa6bce66e6bf163d7ae30b75e0fcc5032046','MATERIAL','各实验表格','各实验表格.docx','ff/ff870318f3f81a1d51cc482a5d65aa6bce66e6bf163d7ae30b75e0fcc5032046.docx','previews/ff870318f3f81a1d51cc482a5d65aa6bce66e6bf163d7ae30b75e0fcc5032046.pdf',NULL,NULL,0,14472,'来源：七下（鄞州）/第14课 《物联系统原型的程序编写》/各实验表格.docx','',2
FROM biz_textbook_case c WHERE c.source_key='nb2026c42582e588a8ca1bf0e9b54f8fba2d76'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'a099ce5d721645bfbefa112316b1eec9879744f85f52b35bee2a4757cc084cf7','VIDEO','导入视频','导入视频.mp4','a0/a099ce5d721645bfbefa112316b1eec9879744f85f52b35bee2a4757cc084cf7.mp4','a0/a099ce5d721645bfbefa112316b1eec9879744f85f52b35bee2a4757cc084cf7.mp4',NULL,NULL,0,32538517,'来源：七下（鄞州）/第14课 《物联系统原型的程序编写》/导入视频.mp4','',3
FROM biz_textbook_case c WHERE c.source_key='nb2026c42582e588a8ca1bf0e9b54f8fba2d76'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'fde79346b9cbc0210ebbc36856fd00deea7a95bb79a33e2d57e984eaa0832b5f','WORKSHEET','导学案资料','导学案资料.doc','fd/fde79346b9cbc0210ebbc36856fd00deea7a95bb79a33e2d57e984eaa0832b5f.doc','previews/fde79346b9cbc0210ebbc36856fd00deea7a95bb79a33e2d57e984eaa0832b5f.pdf',NULL,NULL,0,2244648,'来源：七下（鄞州）/第14课 《物联系统原型的程序编写》/导学案/导学案资料.doc','',4
FROM biz_textbook_case c WHERE c.source_key='nb2026c42582e588a8ca1bf0e9b54f8fba2d76'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'d683909a5b4c365a053adf6bcd28c6b7c7e4ca6756bc89a5df507f63efe417b1','WORKSHEET','导学案资料2（前期实验手册）','导学案资料2（前期实验手册）.docx','d6/d683909a5b4c365a053adf6bcd28c6b7c7e4ca6756bc89a5df507f63efe417b1.docx','previews/d683909a5b4c365a053adf6bcd28c6b7c7e4ca6756bc89a5df507f63efe417b1.pdf',NULL,NULL,0,115142,'来源：七下（鄞州）/第14课 《物联系统原型的程序编写》/导学案/导学案资料2（前期实验手册）.docx','',5
FROM biz_textbook_case c WHERE c.source_key='nb2026c42582e588a8ca1bf0e9b54f8fba2d76'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'f2f64e86474f75949d96b0be28aff9fc8569452c63cb7d4a8a6892f5e51e3035','SLIDES','第14课PPT','第14课PPT.pptx','f2/f2f64e86474f75949d96b0be28aff9fc8569452c63cb7d4a8a6892f5e51e3035.pptx','previews/f2f64e86474f75949d96b0be28aff9fc8569452c63cb7d4a8a6892f5e51e3035.pdf',NULL,NULL,0,1480384,'来源：七下（鄞州）/第14课 《物联系统原型的程序编写》/第14课PPT.pptx','',6
FROM biz_textbook_case c WHERE c.source_key='nb2026c42582e588a8ca1bf0e9b54f8fba2d76'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb202681b79bbb037ffbe4304b02a7471dcc4b',b.book_id,'lesson-16','物联系统原型的运行与调试','黄素媛','宁波市曙光中学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-16' AND s.kind='LESSON'
WHERE b.book_code='xxkj-7-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'4e2b935ab32a7c3dbd8873242ea3f5c61c5e7556eb11e405b4a8f8421060e25f','DESIGN','第15课教案','第15课教案.docx','4e/4e2b935ab32a7c3dbd8873242ea3f5c61c5e7556eb11e405b4a8f8421060e25f.docx','previews/4e2b935ab32a7c3dbd8873242ea3f5c61c5e7556eb11e405b4a8f8421060e25f.pdf',NULL,NULL,0,261582,'来源：七下（鄞州）/第15课 物联系统原型的运行与调试/第15课教案.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb202681b79bbb037ffbe4304b02a7471dcc4b'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'b22f939586071a0d0ca8a3d8682b3f006d00cdf17a5c6f8484b5b2f4b2eada96','WORKSHEET','实验报告','实验报告.docx','b2/b22f939586071a0d0ca8a3d8682b3f006d00cdf17a5c6f8484b5b2f4b2eada96.docx','previews/b22f939586071a0d0ca8a3d8682b3f006d00cdf17a5c6f8484b5b2f4b2eada96.pdf',NULL,NULL,0,20167,'来源：七下（鄞州）/第15课 物联系统原型的运行与调试/实验报告.docx','',2
FROM biz_textbook_case c WHERE c.source_key='nb202681b79bbb037ffbe4304b02a7471dcc4b'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'067b7a53c04d14da9f49ef0b781154529d59b9ca6ccdb6dcff034b12bb66fb2c','SLIDES','物联系统原型的运行与调试','物联系统原型的运行与调试.pptx','06/067b7a53c04d14da9f49ef0b781154529d59b9ca6ccdb6dcff034b12bb66fb2c.pptx','previews/067b7a53c04d14da9f49ef0b781154529d59b9ca6ccdb6dcff034b12bb66fb2c.pdf',NULL,NULL,0,3013579,'来源：七下（鄞州）/第15课 物联系统原型的运行与调试/物联系统原型的运行与调试.pptx','',3
FROM biz_textbook_case c WHERE c.source_key='nb202681b79bbb037ffbe4304b02a7471dcc4b'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-d98c3d94152bd576908224e71cbf2c6c976f87910c3f515a4aca4a05616abf31','MATERIAL','二分搜索算法-象山县-第17课《二分搜索算法》-叶晓静','第17课-二分搜索算法-叶晓静-象山县-第17课《二分搜索算法》-叶晓静.zip','d9/d98c3d94152bd576908224e71cbf2c6c976f87910c3f515a4aca4a05616abf31.zip',NULL,NULL,NULL,0,424656,'共 4 个文件，来源目录：七下（鄞州）/象山县-第17课《二分搜索算法》-叶晓静','解压后用浏览器打开入口 HTML；若页面依赖本地服务或模型文件，请按包内 README 启动。',1
FROM biz_textbook_case c WHERE c.source_key='0981b3d5de512c66542d07f49ae1208f'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-e53da6d917e79b172e29ceed00420fbbe418a243bae39560312f34435f02cbed','MATERIAL','深度优先搜索算法-第18课《深度优先搜索算法》','第18课-深度优先搜索算法-夏吕丹-第18课《深度优先搜索算法》.zip','e5/e53da6d917e79b172e29ceed00420fbbe418a243bae39560312f34435f02cbed.zip',NULL,NULL,NULL,0,43027,'共 1 个文件，来源目录：七下（鄞州）/慈溪市-第18课《深度优先搜索算法》','解压后按文件名和包内 README 使用。',1
FROM biz_textbook_case c WHERE c.source_key='460c7b9d75c9353b9a062a93c11ae442'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-0e2f9100d9e42e380e801d7ee5b9b50cd58dfb92d5eaa1bf790e8630a06fb17a','MATERIAL','深度优先搜索算法-第18课《深度优先搜索算法》','第18课-深度优先搜索算法-郑瑾-第18课《深度优先搜索算法》.zip','0e/0e2f9100d9e42e380e801d7ee5b9b50cd58dfb92d5eaa1bf790e8630a06fb17a.zip',NULL,NULL,NULL,0,51754,'共 8 个文件，来源目录：七下（鄞州）/象山县-第18课《深度优先搜索算法》','需 Node.js 环境：解压后在项目目录执行 npm install，再按 README 或 package.json 启动。\n解压后用浏览器打开入口 HTML；若页面依赖本地服务或模型文件，请按包内 README 启动。',1
FROM biz_textbook_case c WHERE c.source_key='fa2cf5a833a5d39128de58ce37120c8b'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-9ff396904aab978a91c58074750d972eb0e651fbe31df321ef626f164e594d1c','MATERIAL','人工智能的技术基础-象山县-第19课《人工智能的技术基础》(1)','第19课-人工智能的技术基础-黄芳芳-象山县-第19课《人工智能的技术基础》(1).zip','9f/9ff396904aab978a91c58074750d972eb0e651fbe31df321ef626f164e594d1c.zip',NULL,NULL,NULL,0,32078429,'共 6 个文件，来源目录：七下（鄞州）/象山县-第19课《人工智能的技术基础》(1)','解压后用浏览器打开入口 HTML；若页面依赖本地服务或模型文件，请按包内 README 启动。\n含 Windows 程序或脚本，解压后按包内说明运行；请先确认来源可信。',1
FROM biz_textbook_case c WHERE c.source_key='312d510591e3cb3d4451502a8b200d5d'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-e3d5a206a3e27a5ff1ec8c8e50dd92a8eed0856b15d2f51723aeabc1edbea2af','MATERIAL','人工智能中的神经网络-象山县-第20课《人工智能中的神经网络》','第20课-人工智能中的神经网络-塔山中学-象山县-第20课《人工智能中的神经网络》.zip','e3/e3d5a206a3e27a5ff1ec8c8e50dd92a8eed0856b15d2f51723aeabc1edbea2af.zip',NULL,NULL,NULL,0,222135913,'共 402 个文件，来源目录：七下（鄞州）/象山县-第20课《人工智能中的神经网络》','解压后按文件名和包内 README 使用。',1
FROM biz_textbook_case c WHERE c.source_key='0dd643de6210d7312321d23d24a01175'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-e4fc834db78895760f8b602110b138cf602cdb39748164d3aa456ecf76b8bfb2','MATERIAL','智能制造-鄞州区-第18课《智能制造》-臧天赐','第18课-智能制造-臧天赐-鄞州区-第18课《智能制造》-臧天赐.zip','e4/e4fc834db78895760f8b602110b138cf602cdb39748164d3aa456ecf76b8bfb2.zip',NULL,NULL,NULL,0,43939545,'共 67 个文件，来源目录：八上/鄞州区-第18课《智能制造》-臧天赐','解压后用浏览器打开入口 HTML；若页面依赖本地服务或模型文件，请按包内 README 启动。',1
FROM biz_textbook_case c WHERE c.source_key='1659aa7a9083b251cf48ec6c219d1e6a'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-b559989b21f1fdfcd416d217e290b4d3e2790662344d6d0c177037ac35444fd5','MATERIAL','智能教育-鄞州区-第19课《智能教育》','第19课-智能教育-黄素媛-鄞州区-第19课《智能教育》.zip','b5/b559989b21f1fdfcd416d217e290b4d3e2790662344d6d0c177037ac35444fd5.zip',NULL,NULL,NULL,0,1810343,'共 5 个文件，来源目录：八上/鄞州区-第19课《智能教育》','解压后用浏览器打开入口 HTML；若页面依赖本地服务或模型文件，请按包内 README 启动。',1
FROM biz_textbook_case c WHERE c.source_key='42f63924c44d931831b853ec31afaf92'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-5d611e3cc550c5e0bc6293515a1e4560d45f11dfd1deec68c667910bdffdd60c','MATERIAL','智能创意-鄞州区-第20课《智能创意》','第20课-智能创意-×××-鄞州区-第20课《智能创意》.zip','5d/5d611e3cc550c5e0bc6293515a1e4560d45f11dfd1deec68c667910bdffdd60c.zip',NULL,NULL,NULL,0,2377374,'共 14 个文件，来源目录：八上/鄞州区-第20课《智能创意》','解压后用浏览器打开入口 HTML；若页面依赖本地服务或模型文件，请按包内 README 启动。',1
FROM biz_textbook_case c WHERE c.source_key='6ed99cb8e7fc36fe808bcfd067d8544c'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb20264ee8357db8128678e205a4c0f7e04d94',b.book_id,'lesson-1','语音识别技术','葛家源','宁海县梅林初级中学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-1' AND s.kind='LESSON'
WHERE b.book_code='xxkj-8-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'be798eafbc5f79dd85bd29523bd1146eedd43390f762efcc049fc68e98ae22fe','DESIGN','第1课《语音识别技术》宁海县梅林中学葛家源','第1课《语音识别技术》宁海县梅林中学葛家源.docx','be/be798eafbc5f79dd85bd29523bd1146eedd43390f762efcc049fc68e98ae22fe.docx','previews/be798eafbc5f79dd85bd29523bd1146eedd43390f762efcc049fc68e98ae22fe.pdf',NULL,NULL,0,40253,'来源：八下（宁海）/第1课《语音识别技术》宁海县梅林中学葛家源.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb20264ee8357db8128678e205a4c0f7e04d94'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb202602a1d9e32c542313327e9374efa0a7da',b.book_id,'lesson-1','语音识别技术','褚泽楷','宁海县城关中学','NINGBO2026','PUBLISHED',102
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-1' AND s.kind='LESSON'
WHERE b.book_code='xxkj-8-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'2b9af1fe4d92b70dd8ffb653eb123c0b7944a0b0f886ae6353d211ba09b0fd01','DESIGN','第1课《语音识别技术》宁海县城关中学褚泽楷','第1课《语音识别技术》宁海县城关中学褚泽楷.docx','2b/2b9af1fe4d92b70dd8ffb653eb123c0b7944a0b0f886ae6353d211ba09b0fd01.docx','previews/2b9af1fe4d92b70dd8ffb653eb123c0b7944a0b0f886ae6353d211ba09b0fd01.pdf',NULL,NULL,0,29681,'来源：八下（宁海）/第1课《语音识别技术》宁海县城关中学褚泽楷.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb202602a1d9e32c542313327e9374efa0a7da'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026c49562799a03dba9bc5aac0efc97a136',b.book_id,'lesson-1','语音识别技术','鲍琼琼','宁海县桃源初级中学','NINGBO2026','PUBLISHED',103
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-1' AND s.kind='LESSON'
WHERE b.book_code='xxkj-8-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'53759b8da616e8f8ad4f3fad65ece73630f4a22eba30cb727cc5e35598dd9d49','DESIGN','第1课《语音识别技术》宁海县桃源中学鲍琼琼','第1课《语音识别技术》宁海县桃源中学鲍琼琼.docx','53/53759b8da616e8f8ad4f3fad65ece73630f4a22eba30cb727cc5e35598dd9d49.docx','previews/53759b8da616e8f8ad4f3fad65ece73630f4a22eba30cb727cc5e35598dd9d49.pdf',NULL,NULL,0,22391,'来源：八下（宁海）/第1课《语音识别技术》宁海县桃源中学鲍琼琼.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026c49562799a03dba9bc5aac0efc97a136'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb20263c5e40f06eb1dbb05cf2cec0bada4c2d',b.book_id,'lesson-2','语音合成技术','华夏','宁海县西店中学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-2' AND s.kind='LESSON'
WHERE b.book_code='xxkj-8-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'b17f0c559e3cbe83001ab5803720db14ba56bd52973181e34396547ea907f41d','DESIGN','第2课《语音合成技术》宁海西店中学华夏','第2课《语音合成技术》宁海西店中学华夏.docx','b1/b17f0c559e3cbe83001ab5803720db14ba56bd52973181e34396547ea907f41d.docx','previews/b17f0c559e3cbe83001ab5803720db14ba56bd52973181e34396547ea907f41d.pdf',NULL,NULL,0,19374,'来源：八下（宁海）/第2课《语音合成技术》宁海西店中学华夏.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb20263c5e40f06eb1dbb05cf2cec0bada4c2d'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026a4ef754b250e3bc3e347f67c8617f422',b.book_id,'lesson-2','语音合成技术','范梦婷','宁海县缑城中学','NINGBO2026','PUBLISHED',102
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-2' AND s.kind='LESSON'
WHERE b.book_code='xxkj-8-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'d4509ce563aece9f4864bee942fe2a3604b12bad96a0d599472ef8e3671a378f','DESIGN','第2课《语音合成技术》宁海县缑城中学范梦婷','第2课《语音合成技术》宁海县缑城中学范梦婷.docx','d4/d4509ce563aece9f4864bee942fe2a3604b12bad96a0d599472ef8e3671a378f.docx','previews/d4509ce563aece9f4864bee942fe2a3604b12bad96a0d599472ef8e3671a378f.pdf',NULL,NULL,0,195877,'来源：八下（宁海）/第2课《语音合成技术》宁海县缑城中学范梦婷.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026a4ef754b250e3bc3e347f67c8617f422'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026d4bc60ad5774ef47bac750d9573d7442',b.book_id,'lesson-3','图像识别技术','王敏瑜','宁海县深甽镇初级中学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-3' AND s.kind='LESSON'
WHERE b.book_code='xxkj-8-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'642871cede192d265baeae4ac3693e58dc0f435f47325665456ccb457adadf54','DESIGN','第3课《图像识别技术》 宁海县深甽中学王敏瑜','第3课《图像识别技术》 宁海县深甽中学王敏瑜.docx','64/642871cede192d265baeae4ac3693e58dc0f435f47325665456ccb457adadf54.docx','previews/642871cede192d265baeae4ac3693e58dc0f435f47325665456ccb457adadf54.pdf',NULL,NULL,0,103252,'来源：八下（宁海）/第3课《图像识别技术》 宁海县深甽中学王敏瑜.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026d4bc60ad5774ef47bac750d9573d7442'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb202639c63e8340d7ab17a143400677707d31',b.book_id,'lesson-3','图像识别技术','的奥秘','刷脸进出学校','NINGBO2026','PUBLISHED',102
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-3' AND s.kind='LESSON'
WHERE b.book_code='xxkj-8-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'5e2d1133f7bf162bc5f30400d11cfb5fd698ae2e4b99d227d7148c0d5226e70f','DESIGN','第3课《图像识别技术》宁海县大佳何镇初级中学王煜华','第3课《图像识别技术》宁海县大佳何镇初级中学王煜华.docx','5e/5e2d1133f7bf162bc5f30400d11cfb5fd698ae2e4b99d227d7148c0d5226e70f.docx','previews/5e2d1133f7bf162bc5f30400d11cfb5fd698ae2e4b99d227d7148c0d5226e70f.pdf',NULL,NULL,0,81253,'来源：八下（宁海）/第3课《图像识别技术》宁海县大佳何镇初级中学王煜华.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb202639c63e8340d7ab17a143400677707d31'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-80a3f4584a82acad46eda482ccc1833040086b0346637b8e15f4d669c8080137','MATERIAL','K近邻算法-慈溪市-第9课《K近邻算法》','第9课-K近邻算法-沈倩-慈溪市-第9课《K近邻算法》.zip','80/80a3f4584a82acad46eda482ccc1833040086b0346637b8e15f4d669c8080137.zip',NULL,NULL,NULL,0,125504,'共 3 个文件，来源目录：八下（宁海）/慈溪市-第9课《K近邻算法》','解压后用浏览器打开入口 HTML；若页面依赖本地服务或模型文件，请按包内 README 启动。',1
FROM biz_textbook_case c WHERE c.source_key='c96f299ddae7717c13b41fe1f7dd2729'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb20266b6110700ead865692fc15c9dd35c5cc',b.book_id,'lesson-16','物联网数据的特点','高晗','宁海县黄坛中学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-16' AND s.kind='LESSON'
WHERE b.book_code='xxkj-8-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'d2218a08853f2c22d621fc9d6ec15e9e4531f37058503bb74eec650e03f9f688','MATERIAL','第16课 《物联网数据的特点》宁海县黄坛中学高晗','第16课 《物联网数据的特点》宁海县黄坛中学高晗.docx','d2/d2218a08853f2c22d621fc9d6ec15e9e4531f37058503bb74eec650e03f9f688.docx','previews/d2218a08853f2c22d621fc9d6ec15e9e4531f37058503bb74eec650e03f9f688.pdf',NULL,NULL,0,217502,'来源：八下（宁海）/第16课 《物联网数据的特点》宁海县黄坛中学高晗.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb20266b6110700ead865692fc15c9dd35c5cc'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb20267d9eada981401a6cfd09a576b3598aee',b.book_id,'lesson-17','智能物联系统的设计','梁钰帛','宁海县未来学校','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-17' AND s.kind='LESSON'
WHERE b.book_code='xxkj-8-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'74bc34875265569391e8373d404e4a30e8154aab0204154834176e033df9a066','DESIGN','第17课《智能物联系统的设计》宁海县未来学校梁钰帛','第17课《智能物联系统的设计》宁海县未来学校梁钰帛.docx','74/74bc34875265569391e8373d404e4a30e8154aab0204154834176e033df9a066.docx','previews/74bc34875265569391e8373d404e4a30e8154aab0204154834176e033df9a066.pdf',NULL,NULL,0,41785,'来源：八下（宁海）/第17课《智能物联系统的设计》宁海县未来学校梁钰帛.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb20267d9eada981401a6cfd09a576b3598aee'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026c43cfd423da20312ddbaaa154e8e5c6d',b.book_id,'lesson-17','智能物联系统的设计','童伟军','宁海县跃龙中学','NINGBO2026','PUBLISHED',102
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-17' AND s.kind='LESSON'
WHERE b.book_code='xxkj-8-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'96428aba1477fe5b936bb355153a6eb3f1793704b85d11f5fb77c1a9d3dc5ed8','DESIGN','第17课《智能物联系统的设计》宁海县跃龙中学童伟军','第17课《智能物联系统的设计》宁海县跃龙中学童伟军.docx','96/96428aba1477fe5b936bb355153a6eb3f1793704b85d11f5fb77c1a9d3dc5ed8.docx','previews/96428aba1477fe5b936bb355153a6eb3f1793704b85d11f5fb77c1a9d3dc5ed8.pdf',NULL,NULL,0,22945,'来源：八下（宁海）/第17课《智能物联系统的设计》宁海县跃龙中学童伟军.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026c43cfd423da20312ddbaaa154e8e5c6d'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb202644fe4c5bce1c9cbf12336430751f968a',b.book_id,'lesson-17','智能物联系统的设计','顾祺加','宁海县茶院中学','NINGBO2026','PUBLISHED',103
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-17' AND s.kind='LESSON'
WHERE b.book_code='xxkj-8-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'ff572aa3c7b540e651ff6b45de5af53e76eeaf09a5218a0e50b1220ac6cf8929','DESIGN','第17课《智能物联系统的设计》宁海县茶院学校顾祺加','第17课《智能物联系统的设计》宁海县茶院学校顾祺加.docx','ff/ff572aa3c7b540e651ff6b45de5af53e76eeaf09a5218a0e50b1220ac6cf8929.docx','previews/ff572aa3c7b540e651ff6b45de5af53e76eeaf09a5218a0e50b1220ac6cf8929.pdf',NULL,NULL,0,264158,'来源：八下（宁海）/第17课《智能物联系统的设计》宁海县茶院学校顾祺加.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb202644fe4c5bce1c9cbf12336430751f968a'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026a3f3b6edddeb3a3a6fc488727f9eabb5',b.book_id,'lesson-18','智能物联系统的硬件搭建','施茜','宁海县跃龙中学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-18' AND s.kind='LESSON'
WHERE b.book_code='xxkj-8-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'99b2300c4b8a47f2d98737540730296d7cef822cb188b04afe22fb7e3166a6c6','DESIGN','第18课《智能物联系统的硬件搭建》宁海县跃龙中学施茜','第18课《智能物联系统的硬件搭建》宁海县跃龙中学施茜.docx','99/99b2300c4b8a47f2d98737540730296d7cef822cb188b04afe22fb7e3166a6c6.docx','previews/99b2300c4b8a47f2d98737540730296d7cef822cb188b04afe22fb7e3166a6c6.pdf',NULL,NULL,0,362123,'来源：八下（宁海）/第18课《智能物联系统的硬件搭建》宁海县跃龙中学施茜.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026a3f3b6edddeb3a3a6fc488727f9eabb5'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb20268817395052ba3f70aefeb596e1dd331f',b.book_id,'lesson-18','智能物联系统的硬件搭建','葛宸辰','宁海县西店镇初级中学','NINGBO2026','PUBLISHED',102
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-18' AND s.kind='LESSON'
WHERE b.book_code='xxkj-8-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'10e2c751030fe21df0aed200d86b02223ef2b41a495874006cd0d2ec63509410','DESIGN','第18课《智能物联系统的硬件搭建》宁海县西店中学葛宸辰','第18课《智能物联系统的硬件搭建》宁海县西店中学葛宸辰.docx','10/10e2c751030fe21df0aed200d86b02223ef2b41a495874006cd0d2ec63509410.docx','previews/10e2c751030fe21df0aed200d86b02223ef2b41a495874006cd0d2ec63509410.pdf',NULL,NULL,0,18759,'来源：八下（宁海）/第18课《智能物联系统的硬件搭建》宁海县西店中学葛宸辰.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb20268817395052ba3f70aefeb596e1dd331f'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb20262b16bebdf3df60eaf9217583ca3a8e3f',b.book_id,'lesson-18','智能物联系统的硬件搭建','金旭华','宁海县星海中学','NINGBO2026','PUBLISHED',103
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-18' AND s.kind='LESSON'
WHERE b.book_code='xxkj-8-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'4e4a230f22a203359a5f654731ef3d5fbc715aadeb865afaeee25b4c20286896','DESIGN','第18课《智能物联系统的硬件搭建》宁海县星海中学金旭华','第18课《智能物联系统的硬件搭建》宁海县星海中学金旭华.docx','4e/4e4a230f22a203359a5f654731ef3d5fbc715aadeb865afaeee25b4c20286896.docx','previews/4e4a230f22a203359a5f654731ef3d5fbc715aadeb865afaeee25b4c20286896.pdf',NULL,NULL,0,493395,'来源：八下（宁海）/第18课《智能物联系统的硬件搭建》宁海县星海中学金旭华.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb20262b16bebdf3df60eaf9217583ca3a8e3f'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb202655c3e40e63e65e93b27961bd07c51f88',b.book_id,'lesson-19','智能物联系统的软件设计','刘洋','宁海县西店中学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-19' AND s.kind='LESSON'
WHERE b.book_code='xxkj-8-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'081fdd90360df99c6d6b4e1b08d25cb6169deb2ecc4373af8d1c8d147b2b2ba0','DESIGN','第19课《智能物联系统的软件编写》宁海县西店中学刘洋','第19课《智能物联系统的软件编写》宁海县西店中学刘洋.docx','08/081fdd90360df99c6d6b4e1b08d25cb6169deb2ecc4373af8d1c8d147b2b2ba0.docx','previews/081fdd90360df99c6d6b4e1b08d25cb6169deb2ecc4373af8d1c8d147b2b2ba0.pdf',NULL,NULL,0,1211732,'来源：八下（宁海）/第19课《智能物联系统的软件编写》宁海县西店中学刘洋.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb202655c3e40e63e65e93b27961bd07c51f88'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb202646384ca8fb7e10410f996713514d26e1',b.book_id,'lesson-19','智能物联系统的软件设计','师庆良','宁海县跃龙中学','NINGBO2026','PUBLISHED',102
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-19' AND s.kind='LESSON'
WHERE b.book_code='xxkj-8-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'bb19c5db8a9c54a5a693318832b7c2e2c190694cb8f300ab38562cc1eb844a10','DESIGN','第19课《智能物联系统的软件编写》宁海县跃龙中学师庆良','第19课《智能物联系统的软件编写》宁海县跃龙中学师庆良.docx','bb/bb19c5db8a9c54a5a693318832b7c2e2c190694cb8f300ab38562cc1eb844a10.docx','previews/bb19c5db8a9c54a5a693318832b7c2e2c190694cb8f300ab38562cc1eb844a10.pdf',NULL,NULL,0,21446,'来源：八下（宁海）/第19课《智能物联系统的软件编写》宁海县跃龙中学师庆良.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb202646384ca8fb7e10410f996713514d26e1'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb202634c26fd0cdb2445a403b41b82452dbfb',b.book_id,'lesson-20','智能物联系统的调试与完善','秦珍妮','宁海县桃源初级中学','NINGBO2026','PUBLISHED',101
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-20' AND s.kind='LESSON'
WHERE b.book_code='xxkj-8-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'badbef87759d4f8bc5bae07869fa1d41e03a824d131f1f4947263c3833fbf66d','DESIGN','第20课《智能物联系统的调试与完善》宁海县桃源中学秦珍妮','第20课《智能物联系统的调试与完善》宁海县桃源中学秦珍妮.docx','ba/badbef87759d4f8bc5bae07869fa1d41e03a824d131f1f4947263c3833fbf66d.docx','previews/badbef87759d4f8bc5bae07869fa1d41e03a824d131f1f4947263c3833fbf66d.pdf',NULL,NULL,0,20707,'来源：八下（宁海）/第20课《智能物联系统的调试与完善》宁海县桃源中学秦珍妮.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb202634c26fd0cdb2445a403b41b82452dbfb'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case (source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2026a6e80ae1aa2ec6a06ef51791f33222c6',b.book_id,'lesson-20','智能物联系统的调试与完善','陈小科','宁海县力洋镇初级中学','NINGBO2026','PUBLISHED',102
FROM biz_textbook_book b
JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-20' AND s.kind='LESSON'
WHERE b.book_code='xxkj-8-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin='NINGBO2026',status='PUBLISHED',sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'406ee25e676c43b3254212057a02695bca52b34eefb9b01329179fd2e7f0d652','DESIGN','第20课《智能物联系统的调式与完善》宁海县力洋中学陈小科','第20课《智能物联系统的调式与完善》宁海县力洋中学陈小科.docx','40/406ee25e676c43b3254212057a02695bca52b34eefb9b01329179fd2e7f0d652.docx','previews/406ee25e676c43b3254212057a02695bca52b34eefb9b01329179fd2e7f0d652.pdf',NULL,NULL,0,63669,'来源：八下（宁海）/第20课《智能物联系统的调式与完善》宁海县力洋中学陈小科.docx','',1
FROM biz_textbook_case c WHERE c.source_key='nb2026a6e80ae1aa2ec6a06ef51791f33222c6'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_case_resource (case_id,resource_key,category,title,filename,original_path,preview_path,extra_filename,extra_path,extra_bytes,bytes,description,requirements,sort_no)
SELECT c.case_id,'pkg-20694059d7dc3822be9fa08bade5e0d46ac3fb2000ba7f0a3716393b8c4ffb02','MATERIAL','推荐系统探究-象山县 郑琦 -第13课《推荐系统探究》','第13课-推荐系统探究-郑琦-象山县 郑琦 -第13课《推荐系统探究》.zip','20/20694059d7dc3822be9fa08bade5e0d46ac3fb2000ba7f0a3716393b8c4ffb02.zip',NULL,NULL,NULL,0,58952906,'共 48 个文件，来源目录：九年级/象山县 郑琦 -第13课《推荐系统探究》','解压后用浏览器打开入口 HTML；若页面依赖本地服务或模型文件，请按包内 README 启动。\n含 Windows 程序或脚本，解压后按包内说明运行；请先确认来源可信。',1
FROM biz_textbook_case c WHERE c.source_key='fa2dab773e1e898d2102fa96a99d5605'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),extra_filename=VALUES(extra_filename),extra_path=VALUES(extra_path),extra_bytes=VALUES(extra_bytes),bytes=VALUES(bytes),description=VALUES(description),requirements=VALUES(requirements),sort_no=VALUES(sort_no);

INSERT INTO biz_textbook_book_resource (book_id,resource_key,category,title,filename,original_path,preview_path,bytes,description,requirements,sort_no)
SELECT b.book_id,'book-design-xxkj-3-0-2026','DESIGN','整册教学设计汇编','2026义务教育信息科技三年级上册教学设计.pdf','7a/7ae8a136d58c0c1af65d50a0b9a681322f92573a528389c5a4db80bff77f8128.pdf','7a/7ae8a136d58c0c1af65d50a0b9a681322f92573a528389c5a4db80bff77f8128.pdf',3160711,'宁波市 2026 新教材整册教学设计汇编，收录 16 课；与“下载整册”教材 PDF 不同，本文件是教学设计合集。',NULL,10
FROM biz_textbook_book b WHERE b.book_code='xxkj-3-0-2026'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),description=VALUES(description),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_book_resource (book_id,resource_key,category,title,filename,original_path,preview_path,bytes,description,requirements,sort_no)
SELECT b.book_id,'book-design-xxkj-3-1-2026','DESIGN','整册教学设计汇编','2026义务教育信息科技三年级下册教学设计.pdf','c0/c01c78f241ee931a4128cf106e15484b1737385f333c29e8facdd38cf98a7607.pdf','c0/c01c78f241ee931a4128cf106e15484b1737385f333c29e8facdd38cf98a7607.pdf',1403632,'宁波市 2026 新教材整册教学设计汇编，收录 13 课；与“下载整册”教材 PDF 不同，本文件是教学设计合集。',NULL,10
FROM biz_textbook_book b WHERE b.book_code='xxkj-3-1-2026'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),description=VALUES(description),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_book_resource (book_id,resource_key,category,title,filename,original_path,preview_path,bytes,description,requirements,sort_no)
SELECT b.book_id,'book-design-xxkj-4-0-2026','DESIGN','整册教学设计汇编','2026义务教育信息科技四年级上册教学设计.pdf','09/09724f27d386918134cef35550869e25c8e045204aa2e47dcc7802dec502b3fe.pdf','09/09724f27d386918134cef35550869e25c8e045204aa2e47dcc7802dec502b3fe.pdf',13476922,'宁波市 2026 新教材整册教学设计汇编，收录 19 课；与“下载整册”教材 PDF 不同，本文件是教学设计合集。',NULL,10
FROM biz_textbook_book b WHERE b.book_code='xxkj-4-0-2026'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),description=VALUES(description),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_book_resource (book_id,resource_key,category,title,filename,original_path,preview_path,bytes,description,requirements,sort_no)
SELECT b.book_id,'book-design-xxkj-4-1-2026','DESIGN','整册教学设计汇编','2026义务教育信息科技四年级下册教学设计.pdf','9a/9afa813104f80690419da8dd021d0194f9150d0568acc60128e167b0c32f7a75.pdf','9a/9afa813104f80690419da8dd021d0194f9150d0568acc60128e167b0c32f7a75.pdf',6112343,'宁波市 2026 新教材整册教学设计汇编，收录 18 课；与“下载整册”教材 PDF 不同，本文件是教学设计合集。',NULL,10
FROM biz_textbook_book b WHERE b.book_code='xxkj-4-1-2026'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),description=VALUES(description),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_book_resource (book_id,resource_key,category,title,filename,original_path,preview_path,bytes,description,requirements,sort_no)
SELECT b.book_id,'book-design-xxkj-5-0-2026','DESIGN','整册教学设计汇编','2026义务教育信息科技五年级上册教学设计.pdf','2b/2b9a58abf88fda0d39d2c8031ba1987f37d0a17b806a964ce800b394fe8d8a65.pdf','2b/2b9a58abf88fda0d39d2c8031ba1987f37d0a17b806a964ce800b394fe8d8a65.pdf',3794443,'宁波市 2026 新教材整册教学设计汇编，收录 16 课；与“下载整册”教材 PDF 不同，本文件是教学设计合集。',NULL,10
FROM biz_textbook_book b WHERE b.book_code='xxkj-5-0-2026'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),description=VALUES(description),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_book_resource (book_id,resource_key,category,title,filename,original_path,preview_path,bytes,description,requirements,sort_no)
SELECT b.book_id,'book-design-xxkj-5-1-2026','DESIGN','整册教学设计汇编','2026义务教育信息科技五年级下册教学设计.pdf','8d/8dda575c59e715e7ac2a4b650a85c2731023810eb438f5eaa754234c468264ea.pdf','8d/8dda575c59e715e7ac2a4b650a85c2731023810eb438f5eaa754234c468264ea.pdf',7176671,'宁波市 2026 新教材整册教学设计汇编，收录 18 课；与“下载整册”教材 PDF 不同，本文件是教学设计合集。',NULL,10
FROM biz_textbook_book b WHERE b.book_code='xxkj-5-1-2026'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),description=VALUES(description),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_book_resource (book_id,resource_key,category,title,filename,original_path,preview_path,bytes,description,requirements,sort_no)
SELECT b.book_id,'book-design-xxkj-6-0-2026','DESIGN','整册教学设计汇编','2026义务教育信息科技六年级上册教学设计.pdf','17/178fc6f8b0ab740ffe87c4f68e522e6433ef4a1b185679f1611abf8d27e35472.pdf','17/178fc6f8b0ab740ffe87c4f68e522e6433ef4a1b185679f1611abf8d27e35472.pdf',15426418,'宁波市 2026 新教材整册教学设计汇编，收录 17 课；与“下载整册”教材 PDF 不同，本文件是教学设计合集。',NULL,10
FROM biz_textbook_book b WHERE b.book_code='xxkj-6-0-2026'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),description=VALUES(description),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_book_resource (book_id,resource_key,category,title,filename,original_path,preview_path,bytes,description,requirements,sort_no)
SELECT b.book_id,'book-design-xxkj-6-1-2026','DESIGN','整册教学设计汇编','2026义务教育信息科技六年级下册教学设计.pdf','f0/f0bd138551489bf483fb507279a208485cdfa6b0be065623e056aa3dc0537ed8.pdf','f0/f0bd138551489bf483fb507279a208485cdfa6b0be065623e056aa3dc0537ed8.pdf',6910830,'宁波市 2026 新教材整册教学设计汇编，收录 18 课；与“下载整册”教材 PDF 不同，本文件是教学设计合集。',NULL,10
FROM biz_textbook_book b WHERE b.book_code='xxkj-6-1-2026'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),description=VALUES(description),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_book_resource (book_id,resource_key,category,title,filename,original_path,preview_path,bytes,description,requirements,sort_no)
SELECT b.book_id,'book-design-xxkj-7-0-2026','DESIGN','整册教学设计汇编','2026义务教育信息科技七年级上册教学设计.pdf','af/af5a67329e062c5a0facf0d08d27ec21764edc64e5b86ea02715df1e7836d60b.pdf','af/af5a67329e062c5a0facf0d08d27ec21764edc64e5b86ea02715df1e7836d60b.pdf',9529335,'宁波市 2026 新教材整册教学设计汇编，收录 20 课；与“下载整册”教材 PDF 不同，本文件是教学设计合集。',NULL,10
FROM biz_textbook_book b WHERE b.book_code='xxkj-7-0-2026'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),description=VALUES(description),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_book_resource (book_id,resource_key,category,title,filename,original_path,preview_path,bytes,description,requirements,sort_no)
SELECT b.book_id,'book-design-xxkj-7-1-2026','DESIGN','整册教学设计汇编','2026义务教育信息科技七年级下册教学设计.pdf','01/017e9a6a361ce08c639b6a575cda3def895548581ce499b5ab46e078c21383ed.pdf','01/017e9a6a361ce08c639b6a575cda3def895548581ce499b5ab46e078c21383ed.pdf',6210039,'宁波市 2026 新教材整册教学设计汇编，收录 22 课；与“下载整册”教材 PDF 不同，本文件是教学设计合集。',NULL,10
FROM biz_textbook_book b WHERE b.book_code='xxkj-7-1-2026'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),description=VALUES(description),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_book_resource (book_id,resource_key,category,title,filename,original_path,preview_path,bytes,description,requirements,sort_no)
SELECT b.book_id,'book-design-xxkj-8-0-2026','DESIGN','整册教学设计汇编','2026义务教育信息科技八年级上册教学设计.pdf','52/52d2e5dab09b9fe8b32e6dd0051e7fe12c5b82540433101fe8ff33f1cc671e86.pdf','52/52d2e5dab09b9fe8b32e6dd0051e7fe12c5b82540433101fe8ff33f1cc671e86.pdf',708678,'宁波市 2026 新教材整册教学设计汇编，收录 4 课；与“下载整册”教材 PDF 不同，本文件是教学设计合集。',NULL,10
FROM biz_textbook_book b WHERE b.book_code='xxkj-8-0-2026'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),description=VALUES(description),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_book_resource (book_id,resource_key,category,title,filename,original_path,preview_path,bytes,description,requirements,sort_no)
SELECT b.book_id,'book-design-xxkj-8-1-2026','DESIGN','整册教学设计汇编','2026义务教育信息科技八年级下册教学设计.pdf','e2/e21391bd4cfd426b9e0ffd1ed0fc459baa1530b2edf6df6cf41604c8061883f3.pdf','e2/e21391bd4cfd426b9e0ffd1ed0fc459baa1530b2edf6df6cf41604c8061883f3.pdf',3640070,'宁波市 2026 新教材整册教学设计汇编，收录 10 课；与“下载整册”教材 PDF 不同，本文件是教学设计合集。',NULL,10
FROM biz_textbook_book b WHERE b.book_code='xxkj-8-1-2026'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),description=VALUES(description),sort_no=VALUES(sort_no);
INSERT INTO biz_textbook_book_resource (book_id,resource_key,category,title,filename,original_path,preview_path,bytes,description,requirements,sort_no)
SELECT b.book_id,'book-design-xxkj-9-9-2026','DESIGN','整册教学设计汇编','2026义务教育信息科技九年级教学设计.pdf','81/81d3ad4321b705dd6fb92d8054d8a2502c106ad0e8f3a289b790fe99821b5482.pdf','81/81d3ad4321b705dd6fb92d8054d8a2502c106ad0e8f3a289b790fe99821b5482.pdf',511012,'宁波市 2026 新教材整册教学设计汇编，收录 1 课；与“下载整册”教材 PDF 不同，本文件是教学设计合集。',NULL,10
FROM biz_textbook_book b WHERE b.book_code='xxkj-9-9-2026'
ON DUPLICATE KEY UPDATE category=VALUES(category),title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),description=VALUES(description),sort_no=VALUES(sort_no);

INSERT INTO biz_guide_grade (grade,title,pdf_filename,pdf_path,pdf_bytes,pdf_sha256,usage_filename,usage_path,usage_bytes,note,sort_no,status)
VALUES (3,'在线学习与生活 三年级全一册','1.义务教育信息科技课程教学指南 在线学习与生活 三年级全一册 200dpi_1724057951595.pdf','60/60608a3d6be8bdee7398460a84f2dc68f7262c967da8796a628aac4f9791b54d.pdf',10488221,'60608a3d6be8bdee7398460a84f2dc68f7262c967da8796a628aac4f9791b54d','5eafc49ed271e5cfe9320cf6deae0a53895cc7d75af73c653033cbde3b350952.pdf','5e/5eafc49ed271e5cfe9320cf6deae0a53895cc7d75af73c653033cbde3b350952.pdf',126796,'指南 PDF 在线阅读或下载；配套课件按课次编号排序。',3,'0')
ON DUPLICATE KEY UPDATE title=VALUES(title),pdf_filename=VALUES(pdf_filename),pdf_path=VALUES(pdf_path),pdf_bytes=VALUES(pdf_bytes),pdf_sha256=VALUES(pdf_sha256),usage_filename=VALUES(usage_filename),usage_path=VALUES(usage_path),usage_bytes=VALUES(usage_bytes),note=VALUES(note),sort_no=VALUES(sort_no),status='0';
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,1,'寻找信息科技','第1课 寻找信息科技_1724058304757.pptx','f4/f4d99354fdb6d4a8db43b0250fdfaa589551e82803057f090abf5af81671000b.pptx','previews/f4d99354fdb6d4a8db43b0250fdfaa589551e82803057f090abf5af81671000b.pdf',92703907,'f4d99354fdb6d4a8db43b0250fdfaa589551e82803057f090abf5af81671000b',1,'',1
FROM biz_guide_grade g WHERE g.grade=3
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,2,'使用数字设备','第2课 使用数字设备_1724058305042.pptx','f0/f03cddb378f5e0a4f41a221c00373cd59a6ab48e6c3818846e1af26eb6ae26a5.pptx','previews/f03cddb378f5e0a4f41a221c00373cd59a6ab48e6c3818846e1af26eb6ae26a5.pdf',54791522,'f03cddb378f5e0a4f41a221c00373cd59a6ab48e6c3818846e1af26eb6ae26a5',1,'',2
FROM biz_guide_grade g WHERE g.grade=3
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,3,'体验人机交互','第3课 体验人机交互_1724058305239.pptx','60/60ab680eae557d8675e1bdea236fbb7aae81b8f8c53fcf192111372d7a50832f.pptx','previews/60ab680eae557d8675e1bdea236fbb7aae81b8f8c53fcf192111372d7a50832f.pdf',24322511,'60ab680eae557d8675e1bdea236fbb7aae81b8f8c53fcf192111372d7a50832f',1,'',3
FROM biz_guide_grade g WHERE g.grade=3
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,4,'图片记录瞬间','第4课 图片记录瞬间_1724058306661.pptx','32/325efb94c9b16767cf5ba42097cf9ef19f153b6e52e7d100d8e951795905fe76.pptx','previews/325efb94c9b16767cf5ba42097cf9ef19f153b6e52e7d100d8e951795905fe76.pdf',88827726,'325efb94c9b16767cf5ba42097cf9ef19f153b6e52e7d100d8e951795905fe76',1,'',4
FROM biz_guide_grade g WHERE g.grade=3
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,5,'美化处理图片','第5课 美化处理图片_1724058307141.pptx','03/03d5a0fe96ba18393a5c69c10d2f922d97b4c09c8dc5e7164fcbc055b746dadd.pptx','previews/03d5a0fe96ba18393a5c69c10d2f922d97b4c09c8dc5e7164fcbc055b746dadd.pdf',36826478,'03d5a0fe96ba18393a5c69c10d2f922d97b4c09c8dc5e7164fcbc055b746dadd',1,'',5
FROM biz_guide_grade g WHERE g.grade=3
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,6,'视频记录片段','第6课  视频记录片段_1724058309050.pptx','17/17a7bd41744466f86f021774f41d568e75aec5ecda7d479d90b702f5fad6c9c8.pptx','previews/17a7bd41744466f86f021774f41d568e75aec5ecda7d479d90b702f5fad6c9c8.pdf',47697683,'17a7bd41744466f86f021774f41d568e75aec5ecda7d479d90b702f5fad6c9c8',1,'',6
FROM biz_guide_grade g WHERE g.grade=3
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,7,'音频记录声音','第7课  音频记录声音_1724058309289.pptx','2a/2ad411929600197ff1c4e7b4b915e48c150b84a3183e8f713824cc90fed76e2a.pptx','previews/2ad411929600197ff1c4e7b4b915e48c150b84a3183e8f713824cc90fed76e2a.pdf',9698518,'2ad411929600197ff1c4e7b4b915e48c150b84a3183e8f713824cc90fed76e2a',1,'',7
FROM biz_guide_grade g WHERE g.grade=3
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,8,'浏览网络资源','第8课  浏览网络资源_1724058310491.pptx','5f/5f2584623de94c3565e68a878b69623df525019f68a4ab95b70aaacae2516187.pptx','previews/5f2584623de94c3565e68a878b69623df525019f68a4ab95b70aaacae2516187.pdf',10353626,'5f2584623de94c3565e68a878b69623df525019f68a4ab95b70aaacae2516187',1,'',8
FROM biz_guide_grade g WHERE g.grade=3
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,9,'在线搜索资源','第9课  在线搜索资源_1724058311589.pptx','2a/2ac8214cb56d842d82e7f5001ec8b1f7fc1bc37069e201fef0d254d646a2acce.pptx','previews/2ac8214cb56d842d82e7f5001ec8b1f7fc1bc37069e201fef0d254d646a2acce.pdf',16130735,'2ac8214cb56d842d82e7f5001ec8b1f7fc1bc37069e201fef0d254d646a2acce',1,'',9
FROM biz_guide_grade g WHERE g.grade=3
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,10,'保存信息资源','第10课 保存信息资源_1724058312830.pptx','39/3989b87db99477d6ba696e24ffcb46ff7821c954f37c30a92e32f76e88be80d3.pptx','previews/3989b87db99477d6ba696e24ffcb46ff7821c954f37c30a92e32f76e88be80d3.pdf',6223453,'3989b87db99477d6ba696e24ffcb46ff7821c954f37c30a92e32f76e88be80d3',1,'',10
FROM biz_guide_grade g WHERE g.grade=3
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,11,'整理信息资源','第11课 整理信息资源_1724058313954.pptx','fb/fbd87ca6b58f6b1a562941627c8cef24bd4a147a0f3cab110d370bab2b005292.pptx','previews/fbd87ca6b58f6b1a562941627c8cef24bd4a147a0f3cab110d370bab2b005292.pdf',14382718,'fbd87ca6b58f6b1a562941627c8cef24bd4a147a0f3cab110d370bab2b005292',1,'',11
FROM biz_guide_grade g WHERE g.grade=3
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,12,'认识数字作品','第12课 认识数字作品_1724058315113.pptx','ff/ff577e8cdff6877be588ec20d445d4f22702ebf9745e30348b5c317634ca5134.pptx','previews/ff577e8cdff6877be588ec20d445d4f22702ebf9745e30348b5c317634ca5134.pdf',19180323,'ff577e8cdff6877be588ec20d445d4f22702ebf9745e30348b5c317634ca5134',1,'',12
FROM biz_guide_grade g WHERE g.grade=3
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,13,'制作数字相册','第13课 制作数字相册_1724058316591.pptx','bf/bfb967a55ad88c9eb9a55feea92ac78abc27b7a2945ece3b329d0a472c693c5b.pptx','previews/bfb967a55ad88c9eb9a55feea92ac78abc27b7a2945ece3b329d0a472c693c5b.pdf',22269285,'bfb967a55ad88c9eb9a55feea92ac78abc27b7a2945ece3b329d0a472c693c5b',1,'',13
FROM biz_guide_grade g WHERE g.grade=3
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,14,'制作数字小报','第14课 制作数字小报_1724058318189.pptx','6b/6bec240456496d8387e8bc16dca5aa508ccb86fe5fd378e57c6e6cc5843f37d2.pptx','previews/6bec240456496d8387e8bc16dca5aa508ccb86fe5fd378e57c6e6cc5843f37d2.pdf',4513297,'6bec240456496d8387e8bc16dca5aa508ccb86fe5fd378e57c6e6cc5843f37d2',1,'',14
FROM biz_guide_grade g WHERE g.grade=3
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,15,'展示数字作品','第15课 展示数字作品_1724058319348.pptx','3d/3dc26a8c6ef73be108d32a5a830b1f05bcf580b467306ca9bbf07b4bdf6e5c8e.pptx','previews/3dc26a8c6ef73be108d32a5a830b1f05bcf580b467306ca9bbf07b4bdf6e5c8e.pdf',5489090,'3dc26a8c6ef73be108d32a5a830b1f05bcf580b467306ca9bbf07b4bdf6e5c8e',1,'',15
FROM biz_guide_grade g WHERE g.grade=3
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,16,'畅享在线交流','第16课  畅享在线交流.pptx','fb/fba2df7f9fe9c9859ca18f24fdad5e85aaaff82b3414d3eb4cc3819517919e7d.pptx','previews/fba2df7f9fe9c9859ca18f24fdad5e85aaaff82b3414d3eb4cc3819517919e7d.pdf',13129409,'fba2df7f9fe9c9859ca18f24fdad5e85aaaff82b3414d3eb4cc3819517919e7d',1,'',16
FROM biz_guide_grade g WHERE g.grade=3
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,17,'参与网络社交','第17课  参与网络社交_1724058320326.pptx','9b/9b2a99f8c319f4e0ee78e37fc46dbbd5372b33e4f8635b23a8ee76959d1d464e.pptx','previews/9b2a99f8c319f4e0ee78e37fc46dbbd5372b33e4f8635b23a8ee76959d1d464e.pdf',9894860,'9b2a99f8c319f4e0ee78e37fc46dbbd5372b33e4f8635b23a8ee76959d1d464e',1,'',17
FROM biz_guide_grade g WHERE g.grade=3
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,18,'在线行为规范','第18课  在线行为规范.pptx','b2/b22ec19ed0b529a3bbc016becb4907e79db25870dac8c7db909a926cf3fdb2ac.pptx','previews/b22ec19ed0b529a3bbc016becb4907e79db25870dac8c7db909a926cf3fdb2ac.pdf',4923064,'b22ec19ed0b529a3bbc016becb4907e79db25870dac8c7db909a926cf3fdb2ac',1,'',18
FROM biz_guide_grade g WHERE g.grade=3
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,19,'认识数字身份','第19课  认识数字身份.pptx','d8/d8452b8ad6d55abbc5ecd86ca7ee1acfb8ae5277eec6bee349a112dfd0d57c40.pptx','previews/d8452b8ad6d55abbc5ecd86ca7ee1acfb8ae5277eec6bee349a112dfd0d57c40.pdf',7296800,'d8452b8ad6d55abbc5ecd86ca7ee1acfb8ae5277eec6bee349a112dfd0d57c40',1,'',19
FROM biz_guide_grade g WHERE g.grade=3
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,20,'体验在线学习','第20课  体验在线学习.pptx','4f/4f86818b5b42380992aa871742248c141dbc170d5715dec87ef06dc71e556fb6.pptx','previews/4f86818b5b42380992aa871742248c141dbc170d5715dec87ef06dc71e556fb6.pdf',6669389,'4f86818b5b42380992aa871742248c141dbc170d5715dec87ef06dc71e556fb6',1,'',20
FROM biz_guide_grade g WHERE g.grade=3
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,21,'分享学习资源','第21课  分享学习资源_1724058321559.pptx','78/78550ddb871888d4fb29b40e5e8bef05ade85a51b184dfb582b361c1c95fa3f7.pptx','previews/78550ddb871888d4fb29b40e5e8bef05ade85a51b184dfb582b361c1c95fa3f7.pdf',7651939,'78550ddb871888d4fb29b40e5e8bef05ade85a51b184dfb582b361c1c95fa3f7',1,'',21
FROM biz_guide_grade g WHERE g.grade=3
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,22,'探讨在线学习','第22课  探讨在线学习.pptx','47/474e6239c492dcca82cca395712d73367e7375e3b9b212f257fdcb730b65184f.pptx','previews/474e6239c492dcca82cca395712d73367e7375e3b9b212f257fdcb730b65184f.pdf',6811387,'474e6239c492dcca82cca395712d73367e7375e3b9b212f257fdcb730b65184f',1,'',22
FROM biz_guide_grade g WHERE g.grade=3
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,23,'分解描述问题','第23课 分解描述问题_1724058322733.pptx','0f/0ff45c379c6f4fbb3d375f4ad2eff8f18c9e9ea58f9f2aca414ef550efa29939.pptx','previews/0ff45c379c6f4fbb3d375f4ad2eff8f18c9e9ea58f9f2aca414ef550efa29939.pdf',4122167,'0ff45c379c6f4fbb3d375f4ad2eff8f18c9e9ea58f9f2aca414ef550efa29939',1,'',23
FROM biz_guide_grade g WHERE g.grade=3
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,24,'分组分派任务','第24课 分组分派任务_1724058323826.pptx','c7/c7e7981cc39779653ab6ee0ae4f1a18214b9c27ac9dbd5890c8022881c007579.pptx','previews/c7e7981cc39779653ab6ee0ae4f1a18214b9c27ac9dbd5890c8022881c007579.pdf',4320146,'c7e7981cc39779653ab6ee0ae4f1a18214b9c27ac9dbd5890c8022881c007579',1,'',24
FROM biz_guide_grade g WHERE g.grade=3
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,25,'合作制作作品','第25课 合作制作作品_1724058324846.pptx','a1/a1a6d1c44e5bb4467f10fd4a5db7b52fe22b5a003b3098fd2af07cb7e6525f01.pptx','previews/a1a6d1c44e5bb4467f10fd4a5db7b52fe22b5a003b3098fd2af07cb7e6525f01.pdf',8527678,'a1a6d1c44e5bb4467f10fd4a5db7b52fe22b5a003b3098fd2af07cb7e6525f01',1,'',25
FROM biz_guide_grade g WHERE g.grade=3
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,26,'完善评价作品','第26课 完善评价作品.pptx','73/7349162baea68d65b23996f3fe0e89d82f0d26c287e1052593b940f8b052b64c.pptx','previews/7349162baea68d65b23996f3fe0e89d82f0d26c287e1052593b940f8b052b64c.pdf',2552114,'7349162baea68d65b23996f3fe0e89d82f0d26c287e1052593b940f8b052b64c',1,'',26
FROM biz_guide_grade g WHERE g.grade=3
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,27,'感悟在线社会','第27课  感悟在线社会_1724058325957.pptx','ca/ca974c70067fadcbf84370c3712532f75a28e38ca2020ec8af6d7b0a837bdf31.pptx','previews/ca974c70067fadcbf84370c3712532f75a28e38ca2020ec8af6d7b0a837bdf31.pdf',84121837,'ca974c70067fadcbf84370c3712532f75a28e38ca2020ec8af6d7b0a837bdf31',1,'',27
FROM biz_guide_grade g WHERE g.grade=3
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,28,'初识人工智能','第28课  初识人工智能_1724058326171.pptx','07/0714129cfd8ea164a1039fb94b3477445256b9cfcafe50093ee1b79e6e027b79.pptx','previews/0714129cfd8ea164a1039fb94b3477445256b9cfcafe50093ee1b79e6e027b79.pdf',45719941,'0714129cfd8ea164a1039fb94b3477445256b9cfcafe50093ee1b79e6e027b79',1,'',28
FROM biz_guide_grade g WHERE g.grade=3
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,29,'了解自主可控','第29课  了解自主可控.pptx','08/08c4b352b4e84cde4ea57f992fb129899dbc9bd765ab7adfa885d98cfa692cec.pptx','previews/08c4b352b4e84cde4ea57f992fb129899dbc9bd765ab7adfa885d98cfa692cec.pdf',40909990,'08c4b352b4e84cde4ea57f992fb129899dbc9bd765ab7adfa885d98cfa692cec',1,'',29
FROM biz_guide_grade g WHERE g.grade=3
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,30,'在线安全防范','第30课  在线安全防范.pptx','36/3681fa1610fe762a397b33393702815752f87fe4619541d2d89d1faa7980c7ef.pptx','previews/3681fa1610fe762a397b33393702815752f87fe4619541d2d89d1faa7980c7ef.pdf',10523812,'3681fa1610fe762a397b33393702815752f87fe4619541d2d89d1faa7980c7ef',1,'',30
FROM biz_guide_grade g WHERE g.grade=3
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);

INSERT INTO biz_guide_grade (grade,title,pdf_filename,pdf_path,pdf_bytes,pdf_sha256,usage_filename,usage_path,usage_bytes,note,sort_no,status)
VALUES (4,'数据与编码 四年级全一册','2.义务教育信息科技课程教学指南 数据与编码 四年级全一册200dpi_1724058291301.pdf','7b/7be8f3d9c0940d572eb6869ec6a934bb9c2af57664ccf83cd01df0d8b28dfdf5.pdf',10774074,'7be8f3d9c0940d572eb6869ec6a934bb9c2af57664ccf83cd01df0d8b28dfdf5','5eafc49ed271e5cfe9320cf6deae0a53895cc7d75af73c653033cbde3b350952.pdf','5e/5eafc49ed271e5cfe9320cf6deae0a53895cc7d75af73c653033cbde3b350952.pdf',126796,'指南 PDF 在线阅读或下载；配套课件按课次编号排序。',4,'0')
ON DUPLICATE KEY UPDATE title=VALUES(title),pdf_filename=VALUES(pdf_filename),pdf_path=VALUES(pdf_path),pdf_bytes=VALUES(pdf_bytes),pdf_sha256=VALUES(pdf_sha256),usage_filename=VALUES(usage_filename),usage_path=VALUES(usage_path),usage_bytes=VALUES(usage_bytes),note=VALUES(note),sort_no=VALUES(sort_no),status='0';
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,1,'数据宝藏在身边','第1课 数据宝藏在身边_1724058326382.pptx','43/43fd15e9309c084a3981427164fcafd3ec62ee298daa0ac87205c30ecafe112a.pptx','previews/43fd15e9309c084a3981427164fcafd3ec62ee298daa0ac87205c30ecafe112a.pdf',3570499,'43fd15e9309c084a3981427164fcafd3ec62ee298daa0ac87205c30ecafe112a',1,'',1
FROM biz_guide_grade g WHERE g.grade=4
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,2,'获取数据有方法','第2课 获取数据有方法.pptx','cb/cb36dd4d614b650e86fb7f7b40c73b4cbc5a5d54e347e748b199797a8f4cb0b5.pptx','previews/cb36dd4d614b650e86fb7f7b40c73b4cbc5a5d54e347e748b199797a8f4cb0b5.pdf',6369833,'cb36dd4d614b650e86fb7f7b40c73b4cbc5a5d54e347e748b199797a8f4cb0b5',1,'',2
FROM biz_guide_grade g WHERE g.grade=4
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,3,'寻找可靠数据源','第3课 寻找可靠数据源.pptx','4b/4bc93278d2321bd35fe461744d2139e2f6c6725db16cf5d5673c39f9eeac40de.pptx','previews/4bc93278d2321bd35fe461744d2139e2f6c6725db16cf5d5673c39f9eeac40de.pdf',3701043,'4bc93278d2321bd35fe461744d2139e2f6c6725db16cf5d5673c39f9eeac40de',1,'',3
FROM biz_guide_grade g WHERE g.grade=4
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,4,'记录数据讲诀窍','第4课 记录数据讲诀窍_1724058327342.pptx','57/573fc6c54878a2cb5753c2867a89ca15525699ea51bb06cf2842e14fab7e115f.pptx','previews/573fc6c54878a2cb5753c2867a89ca15525699ea51bb06cf2842e14fab7e115f.pdf',3793390,'573fc6c54878a2cb5753c2867a89ca15525699ea51bb06cf2842e14fab7e115f',1,'',4
FROM biz_guide_grade g WHERE g.grade=4
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,5,'数据价值巧发现','第5课 数据价值巧发现.pptx','a1/a182d0f3e5e04aa7109a6e78752e7d8e3026e7a08b21051e70ae0dcb0a358430.pptx','previews/a182d0f3e5e04aa7109a6e78752e7d8e3026e7a08b21051e70ae0dcb0a358430.pdf',7304995,'a182d0f3e5e04aa7109a6e78752e7d8e3026e7a08b21051e70ae0dcb0a358430',1,'',5
FROM biz_guide_grade g WHERE g.grade=4
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,6,'古往今来话编码','第6课 古往今来话编码_1724058328369.pptx','77/7777f7c6f79e8ca6c63a7ed9acdd7030a7aaafe27084990e2c7400c5f3cce2ef.pptx','previews/7777f7c6f79e8ca6c63a7ed9acdd7030a7aaafe27084990e2c7400c5f3cce2ef.pdf',4246077,'7777f7c6f79e8ca6c63a7ed9acdd7030a7aaafe27084990e2c7400c5f3cce2ef',1,'',6
FROM biz_guide_grade g WHERE g.grade=4
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,7,'数字编码随处见','第7课 数字编码随处见_1724058329350.pptx','88/889f02deb3dd33fd3fdd38217c14da8bd2b64bfd70c0588a3863e686b3d8688c.pptx','previews/889f02deb3dd33fd3fdd38217c14da8bd2b64bfd70c0588a3863e686b3d8688c.pdf',2481810,'889f02deb3dd33fd3fdd38217c14da8bd2b64bfd70c0588a3863e686b3d8688c',1,'',7
FROM biz_guide_grade g WHERE g.grade=4
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,8,'编码管理我知道','第8课 编码管理我知道_1724058330233.pptx','cf/cfb454881d077d7b583fcdee8045c03866eff507a0e026d718beca493b9b9cb1.pptx','previews/cfb454881d077d7b583fcdee8045c03866eff507a0e026d718beca493b9b9cb1.pdf',2382317,'cfb454881d077d7b583fcdee8045c03866eff507a0e026d718beca493b9b9cb1',1,'',8
FROM biz_guide_grade g WHERE g.grade=4
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,9,'二维码伴我生活','第9课 二维码伴我生活.pptx','22/22e495549bd4eb0fc39492ff6345f6575e506a1306a4a9dfccecca2a7dbea0df.pptx','previews/22e495549bd4eb0fc39492ff6345f6575e506a1306a4a9dfccecca2a7dbea0df.pdf',819222,'22e495549bd4eb0fc39492ff6345f6575e506a1306a4a9dfccecca2a7dbea0df',1,'',9
FROM biz_guide_grade g WHERE g.grade=4
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,10,'数据错误及时验','第10课  数据错误及时验.pptx','09/09048de67d5edcc6e03f2e3bd4a9923d326279f653bf562ce8e02c617126ac47.pptx','previews/09048de67d5edcc6e03f2e3bd4a9923d326279f653bf562ce8e02c617126ac47.pdf',1958378,'09048de67d5edcc6e03f2e3bd4a9923d326279f653bf562ce8e02c617126ac47',1,'',10
FROM biz_guide_grade g WHERE g.grade=4
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,11,'嘀嘀嗒嗒的秘密','第11课 嘀嘀嗒嗒的秘密.pptx','01/01d5b045c356a0847a9047317843b37fc5dfb36ec7e16da3a8bf7413e30d2d58.pptx','previews/01d5b045c356a0847a9047317843b37fc5dfb36ec7e16da3a8bf7413e30d2d58.pdf',1272896,'01d5b045c356a0847a9047317843b37fc5dfb36ec7e16da3a8bf7413e30d2d58',1,'',11
FROM biz_guide_grade g WHERE g.grade=4
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,12,'二值的黑白图像','第12课 二值的黑白图像.pptx','68/6812584155d9a4f181bd94b20b1e3fae3ba18997ebdf8fac11f4d50c6dbda954.pptx','previews/6812584155d9a4f181bd94b20b1e3fae3ba18997ebdf8fac11f4d50c6dbda954.pdf',774968,'6812584155d9a4f181bd94b20b1e3fae3ba18997ebdf8fac11f4d50c6dbda954',1,'',12
FROM biz_guide_grade g WHERE g.grade=4
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,13,'多彩的数字世界','第13课 多彩的数字世界.pptx','d8/d82b22d68f0d82fbf14bf792783e17f9648098d379cadd0d22aa9a710b523f00.pptx','previews/d82b22d68f0d82fbf14bf792783e17f9648098d379cadd0d22aa9a710b523f00.pdf',2713015,'d82b22d68f0d82fbf14bf792783e17f9648098d379cadd0d22aa9a710b523f00',1,'',13
FROM biz_guide_grade g WHERE g.grade=4
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,14,'编码也能动起来','第14课 编码也能动起来_1724058331145.pptx','c0/c001db8b0937c10dbac47b069f47a143105524967f4c5dde08db67c24026bdcd.pptx','previews/c001db8b0937c10dbac47b069f47a143105524967f4c5dde08db67c24026bdcd.pdf',599468,'c001db8b0937c10dbac47b069f47a143105524967f4c5dde08db67c24026bdcd',1,'',14
FROM biz_guide_grade g WHERE g.grade=4
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,15,'自定编码讲规则','第15课 自定编码讲规则.pptx','58/580d175ec6fb9b335aad1c4d533bf7bb8cd5a35e8ce9166bd048bcfba632148f.pptx','previews/580d175ec6fb9b335aad1c4d533bf7bb8cd5a35e8ce9166bd048bcfba632148f.pdf',5586975,'580d175ec6fb9b335aad1c4d533bf7bb8cd5a35e8ce9166bd048bcfba632148f',1,'',15
FROM biz_guide_grade g WHERE g.grade=4
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,16,'数据管理与编码','第16课 数据管理与编码.pptx','f5/f544f13c3394702768a87f5a2574e1e3feb9b4b4f7f158b84fe52e2d4ef1e383.pptx','previews/f544f13c3394702768a87f5a2574e1e3feb9b4b4f7f158b84fe52e2d4ef1e383.pdf',7799711,'f544f13c3394702768a87f5a2574e1e3feb9b4b4f7f158b84fe52e2d4ef1e383',1,'',16
FROM biz_guide_grade g WHERE g.grade=4
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,17,'查找筛选讲效率','第17课 查找筛选讲效率.pptx','bb/bbb9690521a2c81025f4ee55cdb77cc9b930b5fbb7e0c3e75f5f86661c67931a.pptx','previews/bbb9690521a2c81025f4ee55cdb77cc9b930b5fbb7e0c3e75f5f86661c67931a.pdf',6358218,'bbb9690521a2c81025f4ee55cdb77cc9b930b5fbb7e0c3e75f5f86661c67931a',1,'',17
FROM biz_guide_grade g WHERE g.grade=4
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,18,'排序计算有方法','第18课 排序计算有方法.pptx','ff/ff47e2e86e488366103961491f34fd6c396eccbff524ecc994e8f9fdd0c327fb.pptx','previews/ff47e2e86e488366103961491f34fd6c396eccbff524ecc994e8f9fdd0c327fb.pdf',2660631,'ff47e2e86e488366103961491f34fd6c396eccbff524ecc994e8f9fdd0c327fb',1,'',18
FROM biz_guide_grade g WHERE g.grade=4
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,19,'数据安全意识强','第19课 数据安全意识强.pptx','72/7225e63173d3800c333427d8205ea33fd23e57b70170e4db4f65817edf165dea.pptx','previews/7225e63173d3800c333427d8205ea33fd23e57b70170e4db4f65817edf165dea.pdf',4224491,'7225e63173d3800c333427d8205ea33fd23e57b70170e4db4f65817edf165dea',1,'',19
FROM biz_guide_grade g WHERE g.grade=4
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,20,'保护数据小妙招','第20课 保护数据小妙招.pptx','36/36384a37198b6979f214ab39d38a95e9582f71963c435ce5f6435b86f64e2316.pptx','previews/36384a37198b6979f214ab39d38a95e9582f71963c435ce5f6435b86f64e2316.pdf',5074819,'36384a37198b6979f214ab39d38a95e9582f71963c435ce5f6435b86f64e2316',1,'',20
FROM biz_guide_grade g WHERE g.grade=4
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,21,'展现数量的关系','第21课 展现数量的关系.pptx','4d/4dfde44a8b42c8c71b704f3782d7689859a9817bc610dfbc2de71f0aec77a010.pptx','previews/4dfde44a8b42c8c71b704f3782d7689859a9817bc610dfbc2de71f0aec77a010.pdf',923661,'4dfde44a8b42c8c71b704f3782d7689859a9817bc610dfbc2de71f0aec77a010',1,'',21
FROM biz_guide_grade g WHERE g.grade=4
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,22,'探寻趋势与规律','第22课 探寻趋势与规律.pptx','7b/7b092123b80598aa985289749148bb252c052a1eafd3f62a45b0f8cb48954060.pptx','previews/7b092123b80598aa985289749148bb252c052a1eafd3f62a45b0f8cb48954060.pdf',3374128,'7b092123b80598aa985289749148bb252c052a1eafd3f62a45b0f8cb48954060',1,'',22
FROM biz_guide_grade g WHERE g.grade=4
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,23,'挑战多角度比拼','第23课 挑战多角度比拼_1724058332055.pptx','03/034554a223015acd38d3fe6a4ff4b3248812ae5fd63914617c43f2d5d7ec32ba.pptx','previews/034554a223015acd38d3fe6a4ff4b3248812ae5fd63914617c43f2d5d7ec32ba.pdf',1689833,'034554a223015acd38d3fe6a4ff4b3248812ae5fd63914617c43f2d5d7ec32ba',1,'',23
FROM biz_guide_grade g WHERE g.grade=4
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,24,'抽取文本汇词云','第24课 抽取文本汇词云.pptx','7c/7c05fe93a670e6ebc24d6e263e6b4af0769b89bfeb2a7b7d9f7c0c7d31632541.pptx','previews/7c05fe93a670e6ebc24d6e263e6b4af0769b89bfeb2a7b7d9f7c0c7d31632541.pdf',2435082,'7c05fe93a670e6ebc24d6e263e6b4af0769b89bfeb2a7b7d9f7c0c7d31632541',1,'',24
FROM biz_guide_grade g WHERE g.grade=4
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,25,'知识图谱来帮忙','第25课 知识图谱来帮忙.pptx','f5/f57feef5818d6a1b2ea936824b0da0e5211f70ca8bcddeef0c3c8f3746743737.pptx','previews/f57feef5818d6a1b2ea936824b0da0e5211f70ca8bcddeef0c3c8f3746743737.pdf',1923560,'f57feef5818d6a1b2ea936824b0da0e5211f70ca8bcddeef0c3c8f3746743737',1,'',25
FROM biz_guide_grade g WHERE g.grade=4
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,26,'用数据支撑观点','第26课 用数据支撑观点.pptx','aa/aa949680df698be312074573823a8e3afbe928fa6971ba57ff774fc9929ab717.pptx','previews/aa949680df698be312074573823a8e3afbe928fa6971ba57ff774fc9929ab717.pdf',7361704,'aa949680df698be312074573823a8e3afbe928fa6971ba57ff774fc9929ab717',1,'',26
FROM biz_guide_grade g WHERE g.grade=4
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,27,'数据点亮新生活','第27课 数据点亮新生活.pptx','2f/2fbe44e4464aa8220914dd077a0121ee1f41e5053640ca88d962596a3ab65e38.pptx','previews/2fbe44e4464aa8220914dd077a0121ee1f41e5053640ca88d962596a3ab65e38.pdf',11127173,'2fbe44e4464aa8220914dd077a0121ee1f41e5053640ca88d962596a3ab65e38',1,'',27
FROM biz_guide_grade g WHERE g.grade=4
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,28,'大数据助力智能','第28课 大数据助力智能.pptx','48/4801338f082db4054275a0185370f7ef4ef8dfe6dad5437ab3b06ccab22d0d96.pptx','previews/4801338f082db4054275a0185370f7ef4ef8dfe6dad5437ab3b06ccab22d0d96.pdf',4470323,'4801338f082db4054275a0185370f7ef4ef8dfe6dad5437ab3b06ccab22d0d96',1,'',28
FROM biz_guide_grade g WHERE g.grade=4
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,29,'生成式人工智能','第29课 生成式人工智能.pptx','69/691e56b81e3fdd0794c4ec8fd88469dc332360731da3b3b61b74e2d324a37336.pptx','previews/691e56b81e3fdd0794c4ec8fd88469dc332360731da3b3b61b74e2d324a37336.pdf',6172488,'691e56b81e3fdd0794c4ec8fd88469dc332360731da3b3b61b74e2d324a37336',1,'',29
FROM biz_guide_grade g WHERE g.grade=4
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,30,'应对智能新挑战','第30课 应对智能新挑战.pptx','f9/f9538dca8db1a2c8642122950dd42fd1ac244821855bbf2c003ddfa0a1ae8bd4.pptx','previews/f9538dca8db1a2c8642122950dd42fd1ac244821855bbf2c003ddfa0a1ae8bd4.pdf',7422970,'f9538dca8db1a2c8642122950dd42fd1ac244821855bbf2c003ddfa0a1ae8bd4',1,'',30
FROM biz_guide_grade g WHERE g.grade=4
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);

INSERT INTO biz_guide_grade (grade,title,pdf_filename,pdf_path,pdf_bytes,pdf_sha256,usage_filename,usage_path,usage_bytes,note,sort_no,status)
VALUES (5,'身边的算法 五年级全一册','3.义务教育信息科技课程教学指南 身边的算法 五年级全一册 200dpi_1724058296257.pdf','58/587f3cc2d4cc83b8f9e1ebe377b9d97cf95a3e1384fc891e31d1194728bffc3b.pdf',6787465,'587f3cc2d4cc83b8f9e1ebe377b9d97cf95a3e1384fc891e31d1194728bffc3b','5eafc49ed271e5cfe9320cf6deae0a53895cc7d75af73c653033cbde3b350952.pdf','5e/5eafc49ed271e5cfe9320cf6deae0a53895cc7d75af73c653033cbde3b350952.pdf',126796,'指南 PDF 在线阅读或下载；配套课件按课次编号排序。第28课《认识决策树算法》来源资料包未提供课件，页面标注“暂未提供”。',5,'0')
ON DUPLICATE KEY UPDATE title=VALUES(title),pdf_filename=VALUES(pdf_filename),pdf_path=VALUES(pdf_path),pdf_bytes=VALUES(pdf_bytes),pdf_sha256=VALUES(pdf_sha256),usage_filename=VALUES(usage_filename),usage_path=VALUES(usage_path),usage_bytes=VALUES(usage_bytes),note=VALUES(note),sort_no=VALUES(sort_no),status='0';
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,1,'生活处处有算法','第1课 生活处处有算法_1724058332982.pptx','28/284b37471283f832d9cec0ce298afd66266ae20b360f459391617de61bd14c97.pptx','previews/284b37471283f832d9cec0ce298afd66266ae20b360f459391617de61bd14c97.pdf',2360122,'284b37471283f832d9cec0ce298afd66266ae20b360f459391617de61bd14c97',1,'',1
FROM biz_guide_grade g WHERE g.grade=5
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,2,'算法认识与体验','第2课 算法认识与体验_1724058333978.pptx','94/944a35606abe6cdeb8a86c8a6367d971f1143ab6e3fbfae26a91d2a0da0c8c91.pptx','previews/944a35606abe6cdeb8a86c8a6367d971f1143ab6e3fbfae26a91d2a0da0c8c91.pdf',1304835,'944a35606abe6cdeb8a86c8a6367d971f1143ab6e3fbfae26a91d2a0da0c8c91',1,'',2
FROM biz_guide_grade g WHERE g.grade=5
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,3,'游戏体验寻规律','第3课 游戏体验寻规律_1724058334938.pptx','c2/c27fbacef41a4d9702bf18a9629c3383594a5987e5024ddcb322d83cc02a2ee4.pptx','previews/c27fbacef41a4d9702bf18a9629c3383594a5987e5024ddcb322d83cc02a2ee4.pdf',5074830,'c27fbacef41a4d9702bf18a9629c3383594a5987e5024ddcb322d83cc02a2ee4',1,'',3
FROM biz_guide_grade g WHERE g.grade=5
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,4,'算法应用在身边','第4课 算法应用在身边_1724058335942.pptx','c5/c546c8d8ebe6adf4d7542176a02a4f6c5c339d5c33b7ea9965183f8f9306e1b4.pptx','previews/c546c8d8ebe6adf4d7542176a02a4f6c5c339d5c33b7ea9965183f8f9306e1b4.pdf',2173031,'c546c8d8ebe6adf4d7542176a02a4f6c5c339d5c33b7ea9965183f8f9306e1b4',1,'',4
FROM biz_guide_grade g WHERE g.grade=5
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,5,'数学运算讲方法','第5课 数学运算讲方法_1724058336964.pptx','9f/9fc1a17dc0708a04e666fab21470151a0bc372393be4edfad920677e80fe985c.pptx','previews/9fc1a17dc0708a04e666fab21470151a0bc372393be4edfad920677e80fe985c.pdf',994378,'9fc1a17dc0708a04e666fab21470151a0bc372393be4edfad920677e80fe985c',1,'',5
FROM biz_guide_grade g WHERE g.grade=5
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,6,'判断选择用分支','第6课 判断选择用分支_1724058337897.pptx','f7/f7875e71319c3ed31181139d339f74ae1de8898c4863c1cc8a43e437afce3473.pptx','previews/f7875e71319c3ed31181139d339f74ae1de8898c4863c1cc8a43e437afce3473.pdf',1342285,'f7875e71319c3ed31181139d339f74ae1de8898c4863c1cc8a43e437afce3473',1,'',6
FROM biz_guide_grade g WHERE g.grade=5
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,7,'重复操作用循环','第7课 重复操作用循环_1724058338864.pptx','a7/a7d09bf749dabf9618d72e34c6e1924898719dc935d871a6f13842bbb663d28f.pptx','previews/a7d09bf749dabf9618d72e34c6e1924898719dc935d871a6f13842bbb663d28f.pdf',2394947,'a7d09bf749dabf9618d72e34c6e1924898719dc935d871a6f13842bbb663d28f',1,'',7
FROM biz_guide_grade g WHERE g.grade=5
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,8,'算法验证与实现','第8课 算法验证与实现_1724058339812.pptx','be/be2c5784cf010905bdcafbc4be4f9d871457244510a2b4f6807d62715523a02b.pptx','previews/be2c5784cf010905bdcafbc4be4f9d871457244510a2b4f6807d62715523a02b.pdf',1932936,'be2c5784cf010905bdcafbc4be4f9d871457244510a2b4f6807d62715523a02b',1,'',8
FROM biz_guide_grade g WHERE g.grade=5
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,9,'互传密信有诀窍','第9课 互传密信有诀窍_1724058340647.pptx','a2/a2e7a10f20704438c2c68cb49f6545ea9b1af56787ae1077a1e4972fced82d3d.pptx','previews/a2e7a10f20704438c2c68cb49f6545ea9b1af56787ae1077a1e4972fced82d3d.pdf',1096565,'a2e7a10f20704438c2c68cb49f6545ea9b1af56787ae1077a1e4972fced82d3d',1,'',9
FROM biz_guide_grade g WHERE g.grade=5
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,10,'猜数游戏有捷径','第10课 猜数游戏有捷径_1724058341589.pptx','46/460620c69b188f47b4d86eef2809299352b50ab903d42fa8afdf01dae5d80a48.pptx','previews/460620c69b188f47b4d86eef2809299352b50ab903d42fa8afdf01dae5d80a48.pdf',1196651,'460620c69b188f47b4d86eef2809299352b50ab903d42fa8afdf01dae5d80a48',1,'',10
FROM biz_guide_grade g WHERE g.grade=5
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,11,'闰年平年我知道','第11课 闰年平年我知道(1).pptx','d0/d0d8bf7972771cf002c76b8d0f53df3036a7aa7dd69b3d2ac375f862e9e87fed.pptx','previews/d0d8bf7972771cf002c76b8d0f53df3036a7aa7dd69b3d2ac375f862e9e87fed.pdf',934934,'d0d8bf7972771cf002c76b8d0f53df3036a7aa7dd69b3d2ac375f862e9e87fed',1,'',11
FROM biz_guide_grade g WHERE g.grade=5
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,12,'闰年平年我知道','第12课 闰年平年我知道(2）_1724058342530.pptx','47/47282b5d2985d5a89903da2235e1ddea9e23bd4d5fd9b7c1239c2a17ffafe5bc.pptx','previews/47282b5d2985d5a89903da2235e1ddea9e23bd4d5fd9b7c1239c2a17ffafe5bc.pdf',877984,'47282b5d2985d5a89903da2235e1ddea9e23bd4d5fd9b7c1239c2a17ffafe5bc',1,'',12
FROM biz_guide_grade g WHERE g.grade=5
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,13,'让计算机会数数','第13课 让计算机会数数_1724058343425.pptx','3b/3b8d4fa581abfacc0aa5f8b40deedca74bdc63f1f876dcca9b1601a5fb3ac7c0.pptx','previews/3b8d4fa581abfacc0aa5f8b40deedca74bdc63f1f876dcca9b1601a5fb3ac7c0.pdf',2925531,'3b8d4fa581abfacc0aa5f8b40deedca74bdc63f1f876dcca9b1601a5fb3ac7c0',1,'',13
FROM biz_guide_grade g WHERE g.grade=5
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,14,'算法效率比一比','第14课 算法效率比一比.pptx','ce/cea1c2259b0a0b92e2de8965f7ed5df42a18da20eaf1e3b1982ce0a9dec4ae5c.pptx','previews/cea1c2259b0a0b92e2de8965f7ed5df42a18da20eaf1e3b1982ce0a9dec4ae5c.pdf',662678,'cea1c2259b0a0b92e2de8965f7ed5df42a18da20eaf1e3b1982ce0a9dec4ae5c',1,'',14
FROM biz_guide_grade g WHERE g.grade=5
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,15,'简单密码易破解','第15课 简单密码易破解.pptx','e8/e8a85513cd6006dc3a5236b83ac5c0ec3c6615984c4e7e4d7e6e4efe3bbba116.pptx','previews/e8a85513cd6006dc3a5236b83ac5c0ec3c6615984c4e7e4d7e6e4efe3bbba116.pdf',982789,'e8a85513cd6006dc3a5236b83ac5c0ec3c6615984c4e7e4d7e6e4efe3bbba116',1,'',15
FROM biz_guide_grade g WHERE g.grade=5
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,16,'比较交换找最值','第16课  比较交换找最值_1724058344473.pptx','e3/e3eed2629f9e57218f81abfea233cbe1dafb8a1045a1d43fe289d16e3c737944.pptx','previews/e3eed2629f9e57218f81abfea233cbe1dafb8a1045a1d43fe289d16e3c737944.pdf',1359612,'e3eed2629f9e57218f81abfea233cbe1dafb8a1045a1d43fe289d16e3c737944',1,'',16
FROM biz_guide_grade g WHERE g.grade=5
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,17,'选择排序轻松做','第17课 选择排序轻松做.pptx','16/160c7622f8bf83e91b7efb362faeb679eeb4e30ef039a79e9411e1bc34ec8683.pptx','previews/160c7622f8bf83e91b7efb362faeb679eeb4e30ef039a79e9411e1bc34ec8683.pdf',6791556,'160c7622f8bf83e91b7efb362faeb679eeb4e30ef039a79e9411e1bc34ec8683',1,'',17
FROM biz_guide_grade g WHERE g.grade=5
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,18,'冒泡排序齐体验','第18课 冒泡排序齐体验（1）.pptx','2a/2ae491f968c9b59576e607e790bece8b3bf103b56eb50ade486cdbb59ff2ba88.pptx','previews/2ae491f968c9b59576e607e790bece8b3bf103b56eb50ade486cdbb59ff2ba88.pdf',5423937,'2ae491f968c9b59576e607e790bece8b3bf103b56eb50ade486cdbb59ff2ba88',1,'',18
FROM biz_guide_grade g WHERE g.grade=5
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,19,'冒泡排序齐体验','第19课 冒泡排序齐体验（2）.pptx','03/0360a9adea80ae3b87e74e0937d93534b098cbaddac939f5b13b8d740289d797.pptx','previews/0360a9adea80ae3b87e74e0937d93534b098cbaddac939f5b13b8d740289d797.pdf',937867,'0360a9adea80ae3b87e74e0937d93534b098cbaddac939f5b13b8d740289d797',1,'',19
FROM biz_guide_grade g WHERE g.grade=5
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,20,'化大为小桶排序','第20课 化大为小桶排序.pptx','e1/e19271aa7237ef80ea04670d8753329f6a8af13bc56a0b85695ae371c116e380.pptx','previews/e19271aa7237ef80ea04670d8753329f6a8af13bc56a0b85695ae371c116e380.pdf',2432672,'e19271aa7237ef80ea04670d8753329f6a8af13bc56a0b85695ae371c116e380',1,'',20
FROM biz_guide_grade g WHERE g.grade=5
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,21,'鸡兔同笼巧计算','第21课 鸡兔同笼巧计算_1724058345421.pptx','18/18d7a16706629116756cedf79a62d69f798068ab3cbbe7c871b0a225c1f37147.pptx','previews/18d7a16706629116756cedf79a62d69f798068ab3cbbe7c871b0a225c1f37147.pdf',751232,'18d7a16706629116756cedf79a62d69f798068ab3cbbe7c871b0a225c1f37147',1,'',21
FROM biz_guide_grade g WHERE g.grade=5
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,22,'兔子增长有规律','第22课 兔子增长有规律（1）_1724058346320.pptx','7c/7c5cef69c436191de5496ebda49808e49fc884e694b4c0981406de55ee0147e3.pptx','previews/7c5cef69c436191de5496ebda49808e49fc884e694b4c0981406de55ee0147e3.pdf',797786,'7c5cef69c436191de5496ebda49808e49fc884e694b4c0981406de55ee0147e3',1,'',22
FROM biz_guide_grade g WHERE g.grade=5
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,23,'兔子增长有规律','第23课 兔子增长有规律（2）.pptx','f7/f7c1d80cceacc7083227fc34a9ff917c22c054b306f68373688a818fc62d334c.pptx','previews/f7c1d80cceacc7083227fc34a9ff917c22c054b306f68373688a818fc62d334c.pdf',871132,'f7c1d80cceacc7083227fc34a9ff917c22c054b306f68373688a818fc62d334c',1,'',23
FROM biz_guide_grade g WHERE g.grade=5
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,24,'多人过河巧安排','第24课 多人过河巧安排.pptx','a1/a124cf8de54233f2248b6df160ddd15fcef2176ee6339cfdff212736296eaa54.pptx','previews/a124cf8de54233f2248b6df160ddd15fcef2176ee6339cfdff212736296eaa54.pdf',1875022,'a124cf8de54233f2248b6df160ddd15fcef2176ee6339cfdff212736296eaa54',1,'',24
FROM biz_guide_grade g WHERE g.grade=5
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,25,'有趣的七桥问题','第25课 有趣的七桥问题.pptx','e1/e1151369fd8b399d5fb09dfecbc58d1af69bed0886c4afdd606e3f8bc3177d97.pptx','previews/e1151369fd8b399d5fb09dfecbc58d1af69bed0886c4afdd606e3f8bc3177d97.pdf',1285302,'e1151369fd8b399d5fb09dfecbc58d1af69bed0886c4afdd606e3f8bc3177d97',1,'',25
FROM biz_guide_grade g WHERE g.grade=5
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,26,'寻找最短的路径','第26课 寻找最短的路径.pptx','8f/8fad6a6f06ea8e5947eaf18091da4d96e856b4f3a32c9bcdd02ed0805530b905.pptx','previews/8fad6a6f06ea8e5947eaf18091da4d96e856b4f3a32c9bcdd02ed0805530b905.pdf',797537,'8fad6a6f06ea8e5947eaf18091da4d96e856b4f3a32c9bcdd02ed0805530b905',1,'',26
FROM biz_guide_grade g WHERE g.grade=5
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,27,'网页排名有策略','第27课 网页排名有策略.pptx','84/84119acec0b4dc8ddef4396f8014ef7832a098b7ef7241e761e8446930bdb3e8.pptx','previews/84119acec0b4dc8ddef4396f8014ef7832a098b7ef7241e761e8446930bdb3e8.pdf',1530017,'84119acec0b4dc8ddef4396f8014ef7832a098b7ef7241e761e8446930bdb3e8',1,'',27
FROM biz_guide_grade g WHERE g.grade=5
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,28,'认识决策树算法','','',NULL,0,'',0,'来源资料包未提供本课课件；指南目录列有本课，暂未提供。',28
FROM biz_guide_grade g WHERE g.grade=5
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,29,'智能工具再体验','第29课 智能工具再体验.pptx','85/8592d07c30b375379b7258cdc60e91ce5f158741f06ba42605e00e7752955247.pptx','previews/8592d07c30b375379b7258cdc60e91ce5f158741f06ba42605e00e7752955247.pdf',7340257,'8592d07c30b375379b7258cdc60e91ce5f158741f06ba42605e00e7752955247',1,'',29
FROM biz_guide_grade g WHERE g.grade=5
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,30,'生命游戏有规则','第30课 生命游戏有规则.pptx','a7/a792861ae72c23772059ef89edc44fea88b9892c2b6ea59bea51256f30bd93ac.pptx','previews/a792861ae72c23772059ef89edc44fea88b9892c2b6ea59bea51256f30bd93ac.pdf',1077257,'a792861ae72c23772059ef89edc44fea88b9892c2b6ea59bea51256f30bd93ac',1,'',30
FROM biz_guide_grade g WHERE g.grade=5
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);

INSERT INTO biz_guide_grade (grade,title,pdf_filename,pdf_path,pdf_bytes,pdf_sha256,usage_filename,usage_path,usage_bytes,note,sort_no,status)
VALUES (6,'过程与控制 六年级全一册','4.义务教育信息科技课程教学指南 过程与控制 六年级全一册 200dpi_1724058300557.pdf','e5/e5b1fabdb98aee9a246456c338383afabbb12c0878b744533da32a7562c4e400.pdf',33398249,'e5b1fabdb98aee9a246456c338383afabbb12c0878b744533da32a7562c4e400','5eafc49ed271e5cfe9320cf6deae0a53895cc7d75af73c653033cbde3b350952.pdf','5e/5eafc49ed271e5cfe9320cf6deae0a53895cc7d75af73c653033cbde3b350952.pdf',126796,'指南 PDF 在线阅读或下载；配套课件按课次编号排序。',6,'0')
ON DUPLICATE KEY UPDATE title=VALUES(title),pdf_filename=VALUES(pdf_filename),pdf_path=VALUES(pdf_path),pdf_bytes=VALUES(pdf_bytes),pdf_sha256=VALUES(pdf_sha256),usage_filename=VALUES(usage_filename),usage_path=VALUES(usage_path),usage_bytes=VALUES(usage_bytes),note=VALUES(note),sort_no=VALUES(sort_no),status='0';
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,1,'设备控制处处在','第1课  设备控制处处在_1724058347198.pptx','6c/6c0b76d882d8ad673eb3dd41a7c64c59457d5bb08c73a8d2a01a19089cae08e9.pptx','previews/6c0b76d882d8ad673eb3dd41a7c64c59457d5bb08c73a8d2a01a19089cae08e9.pdf',11754735,'6c0b76d882d8ad673eb3dd41a7c64c59457d5bb08c73a8d2a01a19089cae08e9',1,'',1
FROM biz_guide_grade g WHERE g.grade=6
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,2,'一分为二开与关','第2课  一分为二开与关_1724058348365.pptx','f8/f8d62904b5fc13f8a2ccea87844af55d20996c97252acafb7fc0783b78016ecd.pptx','previews/f8d62904b5fc13f8a2ccea87844af55d20996c97252acafb7fc0783b78016ecd.pdf',8380702,'f8d62904b5fc13f8a2ccea87844af55d20996c97252acafb7fc0783b78016ecd',1,'',2
FROM biz_guide_grade g WHERE g.grade=6
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,3,'我是小小控制员','第3课  我是小小控制员.pptx','29/29f2e3ee89f6e62f8ba9ba031a095abde661dd4d34d06e5c2644a3d70f92cc66.pptx','previews/29f2e3ee89f6e62f8ba9ba031a095abde661dd4d34d06e5c2644a3d70f92cc66.pdf',2555029,'29f2e3ee89f6e62f8ba9ba031a095abde661dd4d34d06e5c2644a3d70f92cc66',1,'',3
FROM biz_guide_grade g WHERE g.grade=6
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,4,'输入输出与计算','第4课  输入输出与计算_1724058349481.pptx','5f/5f4e318dba12cb24f186c34532c992cecafe4dbf1292609c9ecce730d2fb180f.pptx','previews/5f4e318dba12cb24f186c34532c992cecafe4dbf1292609c9ecce730d2fb180f.pdf',3811349,'5f4e318dba12cb24f186c34532c992cecafe4dbf1292609c9ecce730d2fb180f',1,'',4
FROM biz_guide_grade g WHERE g.grade=6
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,5,'连续变化的数据','第5课  连续变化的数据_1724058350398.pptx','b6/b60165320c69ca11bf752b287067ff6513eed37faa7aea9a7f3ab82e306d35cb.pptx','previews/b60165320c69ca11bf752b287067ff6513eed37faa7aea9a7f3ab82e306d35cb.pdf',4521245,'b60165320c69ca11bf752b287067ff6513eed37faa7aea9a7f3ab82e306d35cb',1,'',5
FROM biz_guide_grade g WHERE g.grade=6
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,6,'开关量的真与假','第6课  开关量的真与假_1724058351389.pptx','f1/f148bcdc18db280647ab0c03ca4931e1ba66ea39f2c7dfcfe8da36492ef63c89.pptx','previews/f148bcdc18db280647ab0c03ca4931e1ba66ea39f2c7dfcfe8da36492ef63c89.pdf',1794810,'f148bcdc18db280647ab0c03ca4931e1ba66ea39f2c7dfcfe8da36492ef63c89',1,'',6
FROM biz_guide_grade g WHERE g.grade=6
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,7,'开关量的与运算','第7课  开关量的与运算_1724058352302.pptx','a3/a362eaf6a4833ad668e058c1b66c991467ad5a651b1b91c815e454df7f6b9f1b.pptx','previews/a362eaf6a4833ad668e058c1b66c991467ad5a651b1b91c815e454df7f6b9f1b.pdf',1739023,'a362eaf6a4833ad668e058c1b66c991467ad5a651b1b91c815e454df7f6b9f1b',1,'',7
FROM biz_guide_grade g WHERE g.grade=6
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,8,'开关量的或运算','第8课  开关量的或运算_1724058353245.pptx','54/54dc2fa5f9170ff23763fc819eff38aaa1989b63ee273ba5b05b6e22a65655fe.pptx','previews/54dc2fa5f9170ff23763fc819eff38aaa1989b63ee273ba5b05b6e22a65655fe.pdf',2235626,'54dc2fa5f9170ff23763fc819eff38aaa1989b63ee273ba5b05b6e22a65655fe',1,'',8
FROM biz_guide_grade g WHERE g.grade=6
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,9,'从人工到自动化','第９课  从人工到自动化.pptx','28/28261864504c43c9413a6e290d37bf1cc8aa01f12c575c2709a9abab036e9ace.pptx','previews/28261864504c43c9413a6e290d37bf1cc8aa01f12c575c2709a9abab036e9ace.pdf',2873471,'28261864504c43c9413a6e290d37bf1cc8aa01f12c575c2709a9abab036e9ace',1,'',9
FROM biz_guide_grade g WHERE g.grade=6
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,10,'开环控制应用广','第10课  开环控制应用广_1724058354214.pptx','33/33d5ded51a8cdd06aea678bd0035d7ca29a0ae2ff5f8eca795607b13bfbc9a77.pptx','previews/33d5ded51a8cdd06aea678bd0035d7ca29a0ae2ff5f8eca795607b13bfbc9a77.pdf',2582421,'33d5ded51a8cdd06aea678bd0035d7ca29a0ae2ff5f8eca795607b13bfbc9a77',1,'',10
FROM biz_guide_grade g WHERE g.grade=6
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,11,'通过反馈知效果','第11课  通过反馈知效果.pptx','57/5725384bf304ed89bad8d84db2ed842165dfb5986cf80586882abafe0abbb065.pptx','previews/5725384bf304ed89bad8d84db2ed842165dfb5986cf80586882abafe0abbb065.pdf',2307687,'5725384bf304ed89bad8d84db2ed842165dfb5986cf80586882abafe0abbb065',1,'',11
FROM biz_guide_grade g WHERE g.grade=6
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,12,'闭环控制助稳定','第12课  闭环控制助稳定_1724058355233.pptx','97/97bcaf9f6fd7271ed5d94e1c1514f609e5f2aa399fbbbad62ba75136a7913f9c.pptx','previews/97bcaf9f6fd7271ed5d94e1c1514f609e5f2aa399fbbbad62ba75136a7913f9c.pdf',2069079,'97bcaf9f6fd7271ed5d94e1c1514f609e5f2aa399fbbbad62ba75136a7913f9c',1,'',12
FROM biz_guide_grade g WHERE g.grade=6
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,13,'控制系统有特点','第13课  控制系统有特点_1724058356224.pptx','eb/ebcfd40981a16cb9116402ed40c88f23c2b62be8d58421ea5d4f374f1424a7bf.pptx','previews/ebcfd40981a16cb9116402ed40c88f23c2b62be8d58421ea5d4f374f1424a7bf.pdf',6054989,'ebcfd40981a16cb9116402ed40c88f23c2b62be8d58421ea5d4f374f1424a7bf',1,'',13
FROM biz_guide_grade g WHERE g.grade=6
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,14,'复杂系统可分解','第14课  复杂系统可分解_1724058357293.pptx','02/02839864bb9ae61f849c067b9b2a0a5e6c3ed565938f4fe65180d7c9b44e7e4d.pptx','previews/02839864bb9ae61f849c067b9b2a0a5e6c3ed565938f4fe65180d7c9b44e7e4d.pdf',2600213,'02839864bb9ae61f849c067b9b2a0a5e6c3ed565938f4fe65180d7c9b44e7e4d',1,'',14
FROM biz_guide_grade g WHERE g.grade=6
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,15,'模块组装很灵活','第15课  模块组装很灵活_1724058358204.pptx','93/93c0852f854ec87b24c69a7e749ce4eb92f4f72534bc7791d794f0eee6c05522.pptx','previews/93c0852f854ec87b24c69a7e749ce4eb92f4f72534bc7791d794f0eee6c05522.pdf',3252592,'93c0852f854ec87b24c69a7e749ce4eb92f4f72534bc7791d794f0eee6c05522',1,'',15
FROM biz_guide_grade g WHERE g.grade=6
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,16,'智能种植初探密','第16课 智能种植初探密_1724058359208.pptx','07/07bf1086f11ed3e21839c7bbbd7d02d55222308b437f45867e85540bddcba6e0.pptx','previews/07bf1086f11ed3e21839c7bbbd7d02d55222308b437f45867e85540bddcba6e0.pdf',13711007,'07bf1086f11ed3e21839c7bbbd7d02d55222308b437f45867e85540bddcba6e0',1,'',16
FROM biz_guide_grade g WHERE g.grade=6
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,17,'设计我的种植园','第17课 设计我的种植园.pptx','de/de2c4c46aa9fd4e90fe36437e4651839dd2b4c2a2441643e4a760943aea44b6e.pptx','previews/de2c4c46aa9fd4e90fe36437e4651839dd2b4c2a2441643e4a760943aea44b6e.pdf',2058943,'de2c4c46aa9fd4e90fe36437e4651839dd2b4c2a2441643e4a760943aea44b6e',1,'',17
FROM biz_guide_grade g WHERE g.grade=6
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,18,'土壤湿度控制好','第18课 土壤湿度控制好.pptx','69/69b19acb8a7694b21dea525ff1a37b52540bdec5d5b10c187279b684af9168dc.pptx','previews/69b19acb8a7694b21dea525ff1a37b52540bdec5d5b10c187279b684af9168dc.pdf',5549577,'69b19acb8a7694b21dea525ff1a37b52540bdec5d5b10c187279b684af9168dc',1,'',18
FROM biz_guide_grade g WHERE g.grade=6
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,19,'光照温度要适宜','第19课 光照温度要适宜.pptx','3e/3ecf7855b42180319dfa0a4892432db612ec7ca1eb480afb2599b61ad6c4fdc0.pptx','previews/3ecf7855b42180319dfa0a4892432db612ec7ca1eb480afb2599b61ad6c4fdc0.pdf',1726132,'3ecf7855b42180319dfa0a4892432db612ec7ca1eb480afb2599b61ad6c4fdc0',1,'',19
FROM biz_guide_grade g WHERE g.grade=6
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,20,'找找电梯子系统','第20课 找找电梯子系统.pptx','89/89e28c8d5cd624d105e44574677e24606b1c42fd5e7786909ee2e399d7125e76.pptx','previews/89e28c8d5cd624d105e44574677e24606b1c42fd5e7786909ee2e399d7125e76.pdf',12243088,'89e28c8d5cd624d105e44574677e24606b1c42fd5e7786909ee2e399d7125e76',1,'',20
FROM biz_guide_grade g WHERE g.grade=6
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,21,'到达指定的楼层','第21课 到达指定的楼层.pptx','14/1458cef4c13d3e500846bfc0e4faaa5631e55f78f31996e2c954a0c4b65a3182.pptx','previews/1458cef4c13d3e500846bfc0e4faaa5631e55f78f31996e2c954a0c4b65a3182.pdf',3939523,'1458cef4c13d3e500846bfc0e4faaa5631e55f78f31996e2c954a0c4b65a3182',1,'',21
FROM biz_guide_grade g WHERE g.grade=6
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,22,'电梯门的开与关','第22课 电梯门的开与关.pptx','e9/e95b3e4fcf48bf4cf1da86625c096b8e539ae6af45de314ce828b3ae273d1c41.pptx','previews/e95b3e4fcf48bf4cf1da86625c096b8e539ae6af45de314ce828b3ae273d1c41.pdf',2103054,'e95b3e4fcf48bf4cf1da86625c096b8e539ae6af45de314ce828b3ae273d1c41',1,'',22
FROM biz_guide_grade g WHERE g.grade=6
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,23,'如果超载电梯停','第23课 如果超载电梯停.pptx','13/13f1992746ea503715834b1df2342cd4cabbf662ef88d678b8b37a182f5419a9.pptx','previews/13f1992746ea503715834b1df2342cd4cabbf662ef88d678b8b37a182f5419a9.pdf',2097514,'13f1992746ea503715834b1df2342cd4cabbf662ef88d678b8b37a182f5419a9',1,'',23
FROM biz_guide_grade g WHERE g.grade=6
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,24,'自动熄灭转向灯','第24课 自动熄灭转向灯.pptx','f3/f37852cf6246e25711c4e5da669a91bd98dcdc1565d4dbfedb29f769b4b5fde3.pptx','previews/f37852cf6246e25711c4e5da669a91bd98dcdc1565d4dbfedb29f769b4b5fde3.pdf',2233486,'f37852cf6246e25711c4e5da669a91bd98dcdc1565d4dbfedb29f769b4b5fde3',1,'',24
FROM biz_guide_grade g WHERE g.grade=6
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,25,'安全带未系提醒','第25课 安全带未系提醒.pptx','93/93d8764dc54fb1a9e00d7ea48f15ee081aa97082d2f15e357130aad6d69774e7.pptx','previews/93d8764dc54fb1a9e00d7ea48f15ee081aa97082d2f15e357130aad6d69774e7.pdf',1631702,'93d8764dc54fb1a9e00d7ea48f15ee081aa97082d2f15e357130aad6d69774e7',1,'',25
FROM biz_guide_grade g WHERE g.grade=6
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,26,'倒车防撞请注意','第26课 倒车防撞请注意.pptx','6b/6bee18d1aa96d53fbfb660030f91d692b5bbe7daab73d0be3e77c797bd699a1d.pptx','previews/6bee18d1aa96d53fbfb660030f91d692b5bbe7daab73d0be3e77c797bd699a1d.pdf',1647319,'6bee18d1aa96d53fbfb660030f91d692b5bbe7daab73d0be3e77c797bd699a1d',1,'',26
FROM biz_guide_grade g WHERE g.grade=6
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,27,'定速巡航的控制','第27课 定速巡航的控制.pptx','e9/e9ba2c41fd8cff92bc51ab20fca3492b411578bf65bc54f1c259c8ea41c8f56a.pptx','previews/e9ba2c41fd8cff92bc51ab20fca3492b411578bf65bc54f1c259c8ea41c8f56a.pdf',1444615,'e9ba2c41fd8cff92bc51ab20fca3492b411578bf65bc54f1c259c8ea41c8f56a',1,'',27
FROM biz_guide_grade g WHERE g.grade=6
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,28,'使用系统讲安全','第28课 使用系统讲安全.pptx','18/18040502da08b191a98cef533d0eba59b05dbe274b15eab756594e5f85525c4a.pptx','previews/18040502da08b191a98cef533d0eba59b05dbe274b15eab756594e5f85525c4a.pdf',4502548,'18040502da08b191a98cef533d0eba59b05dbe274b15eab756594e5f85525c4a',1,'',28
FROM biz_guide_grade g WHERE g.grade=6
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,29,'避免故障保安全','第29课 避免故障保安全.pptx','46/46152662e2ecc668a9958a931102f7908701dddb98450ebd816eb29248753fad.pptx','previews/46152662e2ecc668a9958a931102f7908701dddb98450ebd816eb29248753fad.pdf',3517807,'46152662e2ecc668a9958a931102f7908701dddb98450ebd816eb29248753fad',1,'',29
FROM biz_guide_grade g WHERE g.grade=6
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,30,'自主可控变强大','第30课 自主可控变强大.pptx','26/26086a1a9b729d6c63c81aa7399eb34d1c47fbce2e87a973469dd11ce5ce6a8d.pptx','previews/26086a1a9b729d6c63c81aa7399eb34d1c47fbce2e87a973469dd11ce5ce6a8d.pdf',1019730,'26086a1a9b729d6c63c81aa7399eb34d1c47fbce2e87a973469dd11ce5ce6a8d',1,'',30
FROM biz_guide_grade g WHERE g.grade=6
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);

INSERT INTO biz_guide_grade (grade,title,pdf_filename,pdf_path,pdf_bytes,pdf_sha256,usage_filename,usage_path,usage_bytes,note,sort_no,status)
VALUES (7,'互联网应用与创新 七年级全一册','5.义务教育信息科技课程教学指南 互联网应用与创新 七年级全一册 200dpi_1724058302383.pdf','a8/a8c03fb30c72a25031b7067dcd652ef35465b608b80769ebf9dbcf03ffff3b13.pdf',12483636,'a8c03fb30c72a25031b7067dcd652ef35465b608b80769ebf9dbcf03ffff3b13','5eafc49ed271e5cfe9320cf6deae0a53895cc7d75af73c653033cbde3b350952.pdf','5e/5eafc49ed271e5cfe9320cf6deae0a53895cc7d75af73c653033cbde3b350952.pdf',126796,'指南 PDF 在线阅读或下载；配套课件按课次编号排序。',7,'0')
ON DUPLICATE KEY UPDATE title=VALUES(title),pdf_filename=VALUES(pdf_filename),pdf_path=VALUES(pdf_path),pdf_bytes=VALUES(pdf_bytes),pdf_sha256=VALUES(pdf_sha256),usage_filename=VALUES(usage_filename),usage_path=VALUES(usage_path),usage_bytes=VALUES(usage_bytes),note=VALUES(note),sort_no=VALUES(sort_no),status='0';
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,1,'互联网发展靠创新','第1课 互联网发展靠创新.pptx','e1/e1a13632e16e857de0a2a826c1afa6b0415767b01f3ca532042da01bf5075af9.pptx','previews/e1a13632e16e857de0a2a826c1afa6b0415767b01f3ca532042da01bf5075af9.pdf',1877965,'e1a13632e16e857de0a2a826c1afa6b0415767b01f3ca532042da01bf5075af9',1,'',1
FROM biz_guide_grade g WHERE g.grade=7
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,2,'互联网应用新特征','第2课 互联网应用新特征.pptx','07/073dd56b846dacb1e49aa6c449aae05ea4394a37f9ab68947d384b1e36ec1e16.pptx','previews/073dd56b846dacb1e49aa6c449aae05ea4394a37f9ab68947d384b1e36ec1e16.pdf',3054539,'073dd56b846dacb1e49aa6c449aae05ea4394a37f9ab68947d384b1e36ec1e16',1,'',2
FROM biz_guide_grade g WHERE g.grade=7
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,3,'互联网影响新体验','第3课 互联网影响新体验.pptx','3b/3b5414003138269f1121449f30c098e2d9342630f6bc21f5466735dac4d08fca.pptx','previews/3b5414003138269f1121449f30c098e2d9342630f6bc21f5466735dac4d08fca.pdf',4242850,'3b5414003138269f1121449f30c098e2d9342630f6bc21f5466735dac4d08fca',1,'',3
FROM biz_guide_grade g WHERE g.grade=7
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,4,'数据分包灵活传','第4课 数据分包灵活传.pptx','67/676a0839fb6246170b47f5ef6c97d505af424b344aa9b19df53347365ae6ac4d.pptx','previews/676a0839fb6246170b47f5ef6c97d505af424b344aa9b19df53347365ae6ac4d.pdf',4213347,'676a0839fb6246170b47f5ef6c97d505af424b344aa9b19df53347365ae6ac4d',1,'',4
FROM biz_guide_grade g WHERE g.grade=7
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,5,'网络协议分层设','第5课 网络协议分层设.pptx','bf/bf596347ad43c8df7f70cf874cc240923c080ea5b505eeb4509d56fc1c664fda.pptx','previews/bf596347ad43c8df7f70cf874cc240923c080ea5b505eeb4509d56fc1c664fda.pdf',963271,'bf596347ad43c8df7f70cf874cc240923c080ea5b505eeb4509d56fc1c664fda',1,'',5
FROM biz_guide_grade g WHERE g.grade=7
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,6,'数字身份辨设备','第6课 数字身份辨设备.pptx','3a/3ad2f11fdbb85e51a96cbce75493f40e1f8c78b84715d81540e4b08076320fd6.pptx','previews/3ad2f11fdbb85e51a96cbce75493f40e1f8c78b84715d81540e4b08076320fd6.pdf',953864,'3ad2f11fdbb85e51a96cbce75493f40e1f8c78b84715d81540e4b08076320fd6',1,'',6
FROM biz_guide_grade g WHERE g.grade=7
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,7,'域名解析换编码','第7课 域名解析换编码_1724058360525.pptx','c5/c536d8c4e2a2c26b426b69201c6f8972866a50e5f94a3a3c96e803d2bc068e44.pptx','previews/c536d8c4e2a2c26b426b69201c6f8972866a50e5f94a3a3c96e803d2bc068e44.pdf',1230609,'c536d8c4e2a2c26b426b69201c6f8972866a50e5f94a3a3c96e803d2bc068e44',1,'',7
FROM biz_guide_grade g WHERE g.grade=7
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,8,'路由路径靠算法','第8课 路由路径靠算法.pptx','75/758ec12c6d89e31999799b739bf91030c2cc1e62af39e65a6857f2783aaa5d3f.pptx','previews/758ec12c6d89e31999799b739bf91030c2cc1e62af39e65a6857f2783aaa5d3f.pdf',2546652,'758ec12c6d89e31999799b739bf91030c2cc1e62af39e65a6857f2783aaa5d3f',1,'',8
FROM biz_guide_grade g WHERE g.grade=7
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,9,'数据传输有新意','第9课 数据传输有新意.pptx','7e/7e1c216dfda06b1a1922d5205426f2c4f31b3a59fc0eeff97aca6f12e9395816.pptx','previews/7e1c216dfda06b1a1922d5205426f2c4f31b3a59fc0eeff97aca6f12e9395816.pdf',1434883,'7e1c216dfda06b1a1922d5205426f2c4f31b3a59fc0eeff97aca6f12e9395816',1,'',9
FROM biz_guide_grade g WHERE g.grade=7
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,10,'综合所学建网络','第10课 综合所学建网络.pptx','70/70f087ab8f28ab6834237954e3c0598f5128e9b4d06112e79f7ad13a43dd7a66.pptx','previews/70f087ab8f28ab6834237954e3c0598f5128e9b4d06112e79f7ad13a43dd7a66.pdf',2157071,'70f087ab8f28ab6834237954e3c0598f5128e9b4d06112e79f7ad13a43dd7a66',1,'',10
FROM biz_guide_grade g WHERE g.grade=7
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,11,'互联网服务应用广','第11课 互联网服务应用广.pptx','20/203a0ec19332cf28c99e1d81146b9f8ed4f365720e7ca598988839e346d90521.pptx','previews/203a0ec19332cf28c99e1d81146b9f8ed4f365720e7ca598988839e346d90521.pdf',3946293,'203a0ec19332cf28c99e1d81146b9f8ed4f365720e7ca598988839e346d90521',1,'',11
FROM biz_guide_grade g WHERE g.grade=7
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,12,'万维网服务大揭秘','第12课 万维网服务大揭秘.pptx','c6/c6836184fda77d2fd36dcca68585e2f6834d945be872c467777f2ed80b5ce50a.pptx','previews/c6836184fda77d2fd36dcca68585e2f6834d945be872c467777f2ed80b5ce50a.pdf',2005860,'c6836184fda77d2fd36dcca68585e2f6834d945be872c467777f2ed80b5ce50a',1,'',12
FROM biz_guide_grade g WHERE g.grade=7
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,13,'万维网安全新协议','第13课 万维网安全新协议.pptx','be/be299f212517018f371cf09a2549db020d1492613410c9ccca21dc822e514fd7.pptx','previews/be299f212517018f371cf09a2549db020d1492613410c9ccca21dc822e514fd7.pdf',1655636,'be299f212517018f371cf09a2549db020d1492613410c9ccca21dc822e514fd7',1,'',13
FROM biz_guide_grade g WHERE g.grade=7
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,14,'互联网搜索新发展','第14课 互联网搜索新发展.pptx','31/3133e3dbdf248baafcc7772d7a885c36d29eed5b37261348cf8f9009b2ed7ec6.pptx','previews/3133e3dbdf248baafcc7772d7a885c36d29eed5b37261348cf8f9009b2ed7ec6.pdf',1750843,'3133e3dbdf248baafcc7772d7a885c36d29eed5b37261348cf8f9009b2ed7ec6',1,'',14
FROM biz_guide_grade g WHERE g.grade=7
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,15,'互联网实验齐发现','第15课 互联网实验齐发现.pptx','20/20a5bca8502f40a067da72c53f3d52fe16c3c0d41aa20eed5095863f88ed8f66.pptx','previews/20a5bca8502f40a067da72c53f3d52fe16c3c0d41aa20eed5095863f88ed8f66.pdf',1331498,'20a5bca8502f40a067da72c53f3d52fe16c3c0d41aa20eed5095863f88ed8f66',1,'',15
FROM biz_guide_grade g WHERE g.grade=7
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,16,'探秘网页与代码','第16课 探秘网页与代码.pptx','d3/d380e4c8080788dc94bec67c7c04642722d15b42de5feeb3d54b865e4850a34e.pptx','previews/d380e4c8080788dc94bec67c7c04642722d15b42de5feeb3d54b865e4850a34e.pdf',2350467,'d380e4c8080788dc94bec67c7c04642722d15b42de5feeb3d54b865e4850a34e',1,'',16
FROM biz_guide_grade g WHERE g.grade=7
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,17,'制作网页展活动','第17课 制作网页展活动.pptx','a6/a6077740670a2534daf4184309675a602cd31b95bc3af75e3eefeb17d092262b.pptx','previews/a6077740670a2534daf4184309675a602cd31b95bc3af75e3eefeb17d092262b.pdf',1632535,'a6077740670a2534daf4184309675a602cd31b95bc3af75e3eefeb17d092262b',1,'',17
FROM biz_guide_grade g WHERE g.grade=7
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,18,'美化网页方法多','第18课 美化网页方法多.pptx','28/28e1f6ea4a5e430ad82cfb72f8dbcdd84977274ece770f3961e0fa954dbfb303.pptx','previews/28e1f6ea4a5e430ad82cfb72f8dbcdd84977274ece770f3961e0fa954dbfb303.pdf',1478333,'28e1f6ea4a5e430ad82cfb72f8dbcdd84977274ece770f3961e0fa954dbfb303',1,'',18
FROM biz_guide_grade g WHERE g.grade=7
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,19,'多人协同效率高','第19课 多人协同效率高.pptx','ca/ca20e9f6f2f5f90a024c09b0c4e2b4401a89506c0bf0ad39e42da5675bf956f4.pptx','previews/ca20e9f6f2f5f90a024c09b0c4e2b4401a89506c0bf0ad39e42da5675bf956f4.pdf',703287,'ca20e9f6f2f5f90a024c09b0c4e2b4401a89506c0bf0ad39e42da5675bf956f4',1,'',19
FROM biz_guide_grade g WHERE g.grade=7
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,20,'组建网站跟我做','第20课 组建网站跟我做.pptx','83/8399aeb6a5296846d88cd2631ed6cc22d7365e734b12f0b2a472f62757c818d2.pptx','previews/8399aeb6a5296846d88cd2631ed6cc22d7365e734b12f0b2a472f62757c818d2.pdf',1097521,'8399aeb6a5296846d88cd2631ed6cc22d7365e734b12f0b2a472f62757c818d2',1,'',20
FROM biz_guide_grade g WHERE g.grade=7
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,21,'移动互联新发展','第21课 移动互联新发展.pptx','6f/6f69ce81a98c0cd929a1304c0f78b8b7b51671620d833ccf4dc150db5718d7d6.pptx','previews/6f69ce81a98c0cd929a1304c0f78b8b7b51671620d833ccf4dc150db5718d7d6.pdf',1373691,'6f69ce81a98c0cd929a1304c0f78b8b7b51671620d833ccf4dc150db5718d7d6',1,'',21
FROM biz_guide_grade g WHERE g.grade=7
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,22,'在线学习新变革','第22课 在线学习新变革.pptx','07/07056baa26ddd6dada90fdde62168f54909f7e901b4f018a3fd0987ef740e178.pptx','previews/07056baa26ddd6dada90fdde62168f54909f7e901b4f018a3fd0987ef740e178.pdf',1980718,'07056baa26ddd6dada90fdde62168f54909f7e901b4f018a3fd0987ef740e178',1,'',22
FROM biz_guide_grade g WHERE g.grade=7
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,23,'在线生活创意多','第23课 在线生活创意多.pptx','02/02324431eb1871806eca8703b1940a1ab1b7f2885432f6e031b5dfcd08201da5.pptx','previews/02324431eb1871806eca8703b1940a1ab1b7f2885432f6e031b5dfcd08201da5.pdf',2988591,'02324431eb1871806eca8703b1940a1ab1b7f2885432f6e031b5dfcd08201da5',1,'',23
FROM biz_guide_grade g WHERE g.grade=7
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,24,'在线交流新气象','第24课 在线交流新气象.pptx','c2/c20a373c95f75297dfa80020a43af8eecadf05d320be8e711e1b81ef7be81baf.pptx','previews/c20a373c95f75297dfa80020a43af8eecadf05d320be8e711e1b81ef7be81baf.pdf',1670213,'c20a373c95f75297dfa80020a43af8eecadf05d320be8e711e1b81ef7be81baf',1,'',24
FROM biz_guide_grade g WHERE g.grade=7
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,25,'网络创新云服务','第25课 网络创新云服务.pptx','01/015bbc4d1023e2db1ded9677c97a85bc5cc2ca0b5568ecb96b31eaa4427c0e22.pptx','previews/015bbc4d1023e2db1ded9677c97a85bc5cc2ca0b5568ecb96b31eaa4427c0e22.pdf',24657443,'015bbc4d1023e2db1ded9677c97a85bc5cc2ca0b5568ecb96b31eaa4427c0e22',1,'',25
FROM biz_guide_grade g WHERE g.grade=7
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,26,'创意展示我家乡','第26课 创意展示我家乡.pptx','ad/ad4be243e5693f92e3130cccc9cd9f881dabb132be3d19ea3223d7716259ecfc.pptx','previews/ad4be243e5693f92e3130cccc9cd9f881dabb132be3d19ea3223d7716259ecfc.pdf',2262707,'ad4be243e5693f92e3130cccc9cd9f881dabb132be3d19ea3223d7716259ecfc',1,'',26
FROM biz_guide_grade g WHERE g.grade=7
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,27,'个人信息防泄露','第27课 个人信息防泄露.pptx','af/afe0ac63878f7e9da9be22aca3d72e2f591ab9d9e9f7c4fc392dc1d09c12a379.pptx','previews/afe0ac63878f7e9da9be22aca3d72e2f591ab9d9e9f7c4fc392dc1d09c12a379.pdf',4220470,'afe0ac63878f7e9da9be22aca3d72e2f591ab9d9e9f7c4fc392dc1d09c12a379',1,'',27
FROM biz_guide_grade g WHERE g.grade=7
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,28,'数字版权要保护','第28课 数字版权要保护.pptx','72/72943a5e73169d3abc224653b6eb9dab487ed31a0b97f93e9585794ac7a33c6c.pptx','previews/72943a5e73169d3abc224653b6eb9dab487ed31a0b97f93e9585794ac7a33c6c.pdf',1070949,'72943a5e73169d3abc224653b6eb9dab487ed31a0b97f93e9585794ac7a33c6c',1,'',28
FROM biz_guide_grade g WHERE g.grade=7
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,29,'企业责任勇担当','第29课 企业责任勇担当.pptx','3f/3f59084439836a7e11bc08d448122bdc9cf9a49bfe5c182bbb8b254524b8f74c.pptx','previews/3f59084439836a7e11bc08d448122bdc9cf9a49bfe5c182bbb8b254524b8f74c.pdf',758142,'3f59084439836a7e11bc08d448122bdc9cf9a49bfe5c182bbb8b254524b8f74c',1,'',29
FROM biz_guide_grade g WHERE g.grade=7
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,30,'网络生态建设好','第30课 网络生态建设好.pptx','fc/fc0addeba8011a7cc103224216a105eeefc9cac471f6f9667bd353fa69cc3443.pptx','previews/fc0addeba8011a7cc103224216a105eeefc9cac471f6f9667bd353fa69cc3443.pdf',1437248,'fc0addeba8011a7cc103224216a105eeefc9cac471f6f9667bd353fa69cc3443',1,'',30
FROM biz_guide_grade g WHERE g.grade=7
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);

INSERT INTO biz_guide_grade (grade,title,pdf_filename,pdf_path,pdf_bytes,pdf_sha256,usage_filename,usage_path,usage_bytes,note,sort_no,status)
VALUES (8,'物联网实践与探索 八年级全一册','6.义务教育信息科技课程教学指南 物联网实践与探索 八年级全一册 200dpi_1724058303571.pdf','9d/9dc961b99564ac5424c63538ec80a410ca4a02acaaa348929db67d2020eecc77.pdf',7813336,'9dc961b99564ac5424c63538ec80a410ca4a02acaaa348929db67d2020eecc77','5eafc49ed271e5cfe9320cf6deae0a53895cc7d75af73c653033cbde3b350952.pdf','5e/5eafc49ed271e5cfe9320cf6deae0a53895cc7d75af73c653033cbde3b350952.pdf',126796,'指南 PDF 在线阅读或下载；配套课件按课次编号排序。',8,'0')
ON DUPLICATE KEY UPDATE title=VALUES(title),pdf_filename=VALUES(pdf_filename),pdf_path=VALUES(pdf_path),pdf_bytes=VALUES(pdf_bytes),pdf_sha256=VALUES(pdf_sha256),usage_filename=VALUES(usage_filename),usage_path=VALUES(usage_path),usage_bytes=VALUES(usage_bytes),note=VALUES(note),sort_no=VALUES(sort_no),status='0';
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,1,'开启物联网之门','第1课 开启物联网之门.pptx','ee/eea81a96be4d299aa19a6b3b23efb211b76eb65fec5e2c205a927854269c58d3.pptx','previews/eea81a96be4d299aa19a6b3b23efb211b76eb65fec5e2c205a927854269c58d3.pdf',10742849,'eea81a96be4d299aa19a6b3b23efb211b76eb65fec5e2c205a927854269c58d3',1,'',1
FROM biz_guide_grade g WHERE g.grade=8
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,2,'传感之古今未来','第2课 传感之古今未来_1724058361523.pptx','35/354b530ac0ffacc6aee4d8c2b78e6ce10d62031f46906567c650b8b961c413d8.pptx','previews/354b530ac0ffacc6aee4d8c2b78e6ce10d62031f46906567c650b8b961c413d8.pdf',12945887,'354b530ac0ffacc6aee4d8c2b78e6ce10d62031f46906567c650b8b961c413d8',1,'',2
FROM biz_guide_grade g WHERE g.grade=8
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,3,'环境数据要感知','第3课 环境数据要感知_1724058362733.pptx','6a/6a8a8dba5dffafd59bea70398c8126a2e601c4c908ee9ab931a592f5e28e0a44.pptx','previews/6a8a8dba5dffafd59bea70398c8126a2e601c4c908ee9ab931a592f5e28e0a44.pdf',118427249,'6a8a8dba5dffafd59bea70398c8126a2e601c4c908ee9ab931a592f5e28e0a44',1,'',3
FROM biz_guide_grade g WHERE g.grade=8
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,4,'物物相连有价值','第4课 物物相连有价值_1724058362935.pptx','17/1723ede71402edb087939a99bd35956025cd8bc2dbc0a6f25a87488aa2aa6c51.pptx','previews/1723ede71402edb087939a99bd35956025cd8bc2dbc0a6f25a87488aa2aa6c51.pdf',4449692,'1723ede71402edb087939a99bd35956025cd8bc2dbc0a6f25a87488aa2aa6c51',1,'',4
FROM biz_guide_grade g WHERE g.grade=8
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,5,'物联功能趣体验','第5课 物联功能趣体验_1724058369812.pptx','d2/d24f7d056e8cdd769d95b0b69500dba8f663ffe84429ef94cc64d0fcc90c8b1c.pptx','previews/d24f7d056e8cdd769d95b0b69500dba8f663ffe84429ef94cc64d0fcc90c8b1c.pdf',6911503,'d24f7d056e8cdd769d95b0b69500dba8f663ffe84429ef94cc64d0fcc90c8b1c',1,'',5
FROM biz_guide_grade g WHERE g.grade=8
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,6,'数据传输方式多','第6课 数据传输方式多_1724058370907.pptx','5d/5dd83787050bc30dd569d6c5031379b8f264161be61032be0db777b63506c3b3.pptx','previews/5dd83787050bc30dd569d6c5031379b8f264161be61032be0db777b63506c3b3.pdf',2387852,'5dd83787050bc30dd569d6c5031379b8f264161be61032be0db777b63506c3b3',1,'',6
FROM biz_guide_grade g WHERE g.grade=8
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,7,'电子标签我揭秘','第7课 电子标签我揭秘.pptx','56/56fa2e1afc8d5fa504e3d92501f99ddc5159044fcf44474cc2583d2d8071133a.pptx','previews/56fa2e1afc8d5fa504e3d92501f99ddc5159044fcf44474cc2583d2d8071133a.pdf',1975345,'56fa2e1afc8d5fa504e3d92501f99ddc5159044fcf44474cc2583d2d8071133a',1,'',7
FROM biz_guide_grade g WHERE g.grade=8
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,8,'巧用蓝牙做工具','第8课 巧用蓝牙做工具_1724058371968.pptx','a8/a8111f784a203d39947da6c5ee9603c9f3707ba726512565cc234484eff517f0.pptx','previews/a8111f784a203d39947da6c5ee9603c9f3707ba726512565cc234484eff517f0.pdf',3411368,'a8111f784a203d39947da6c5ee9603c9f3707ba726512565cc234484eff517f0',1,'',8
FROM biz_guide_grade g WHERE g.grade=8
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,9,'互联协议仍沿用','第9课 互联协议仍沿用_1724058372950.pptx','5f/5fe05cdaa645b45754886d6f546a5695d7a040e94608118359da41844cd81bf7.pptx','previews/5fe05cdaa645b45754886d6f546a5695d7a040e94608118359da41844cd81bf7.pdf',2132244,'5fe05cdaa645b45754886d6f546a5695d7a040e94608118359da41844cd81bf7',1,'',9
FROM biz_guide_grade g WHERE g.grade=8
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,10,'物物互通有新径','第10课  物物互通有新径_1724058374009.pptx','74/743e2cbafc1c9712aef2d3c71cd9bb7342c23f1f544c06206b520088e6c42cd3.pptx','previews/743e2cbafc1c9712aef2d3c71cd9bb7342c23f1f544c06206b520088e6c42cd3.pdf',5141304,'743e2cbafc1c9712aef2d3c71cd9bb7342c23f1f544c06206b520088e6c42cd3',1,'',10
FROM biz_guide_grade g WHERE g.grade=8
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,11,'物联系统细分解','第11课 物联系统细分解_1724058375264.pptx','06/061643f447b5234f21bd8d3a6bf8c623f007b21e2ac061e3867cf92e82214ec7.pptx','previews/061643f447b5234f21bd8d3a6bf8c623f007b21e2ac061e3867cf92e82214ec7.pdf',3796835,'061643f447b5234f21bd8d3a6bf8c623f007b21e2ac061e3867cf92e82214ec7',1,'',11
FROM biz_guide_grade g WHERE g.grade=8
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,12,'刷卡开锁易实现','第12课 刷卡开锁易实现_1724058376250.pptx','b8/b8063ffa28c35ff9efbbf6a8d0b3ecf5500d659cb608f2e970818aff93b47bcc.pptx','previews/b8063ffa28c35ff9efbbf6a8d0b3ecf5500d659cb608f2e970818aff93b47bcc.pdf',10653320,'b8063ffa28c35ff9efbbf6a8d0b3ecf5500d659cb608f2e970818aff93b47bcc',1,'',12
FROM biz_guide_grade g WHERE g.grade=8
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,13,'门铃通知即时到','第13课 门铃通知即时到_1724058377411.pptx','6a/6ae0d0a43e51eccc3aeaa63cc3207e4b4dae2d0753be5ee67a9d151abcf92202.pptx','previews/6ae0d0a43e51eccc3aeaa63cc3207e4b4dae2d0753be5ee67a9d151abcf92202.pdf',3809360,'6ae0d0a43e51eccc3aeaa63cc3207e4b4dae2d0753be5ee67a9d151abcf92202',1,'',13
FROM biz_guide_grade g WHERE g.grade=8
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,14,'远程控制更便捷','第14课 远程控制更便捷.pptx','21/210b5fce31bd9f962a7061022408698697af897add1719901fb761da6cd32cae.pptx','previews/210b5fce31bd9f962a7061022408698697af897add1719901fb761da6cd32cae.pdf',4117462,'210b5fce31bd9f962a7061022408698697af897add1719901fb761da6cd32cae',1,'',14
FROM biz_guide_grade g WHERE g.grade=8
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,15,'面容钥匙显智能','第15课 面容钥匙显智能.pptx','81/8108bcc10855d41ff42e2e189a7726820b7366c1fc7022170cfc8d1ccce6bf25.pptx','previews/8108bcc10855d41ff42e2e189a7726820b7366c1fc7022170cfc8d1ccce6bf25.pdf',69256300,'8108bcc10855d41ff42e2e189a7726820b7366c1fc7022170cfc8d1ccce6bf25',1,'',15
FROM biz_guide_grade g WHERE g.grade=8
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,16,'模块功能先划分','第16课 模块功能先划分_1724058378340.pptx','1e/1e22c2b478d0ff6ed2d3efaae463aa81896d29f1633835dc952cce098211ce2b.pptx','previews/1e22c2b478d0ff6ed2d3efaae463aa81896d29f1633835dc952cce098211ce2b.pdf',3358074,'1e22c2b478d0ff6ed2d3efaae463aa81896d29f1633835dc952cce098211ce2b',1,'',16
FROM biz_guide_grade g WHERE g.grade=8
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,17,'物联数据需采集','第17课 物联数据需采集.pptx','fc/fca959ee76e9ec859f4d01bd6bdcfae5ecbf5bac07faf5a674d37d85196fed68.pptx','previews/fca959ee76e9ec859f4d01bd6bdcfae5ecbf5bac07faf5a674d37d85196fed68.pdf',3379456,'fca959ee76e9ec859f4d01bd6bdcfae5ecbf5bac07faf5a674d37d85196fed68',1,'',17
FROM biz_guide_grade g WHERE g.grade=8
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,18,'数据分析与处理','第18课 数据分析与处理.pptx','cb/cb576a290453347c39f977ad440f1a097eed7c153b8f480f91aeef3f8e07f8de.pptx','previews/cb576a290453347c39f977ad440f1a097eed7c153b8f480f91aeef3f8e07f8de.pdf',2135029,'cb576a290453347c39f977ad440f1a097eed7c153b8f480f91aeef3f8e07f8de',1,'',18
FROM biz_guide_grade g WHERE g.grade=8
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,19,'数据呈现可视化','第19课 数据呈现可视化_1724058379387.pptx','47/479a8d0a98a0663013c7dec8bfbbeb82e5cfa36c2710d96f28f440090cc442a4.pptx','previews/479a8d0a98a0663013c7dec8bfbbeb82e5cfa36c2710d96f28f440090cc442a4.pdf',2793852,'479a8d0a98a0663013c7dec8bfbbeb82e5cfa36c2710d96f28f440090cc442a4',1,'',19
FROM biz_guide_grade g WHERE g.grade=8
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,20,'反馈控制有算法','第20课 反馈控制有算法_1724058380447.pptx','e2/e288713cd934b20229ef4f28eed75d64a2c476808ced52b54f0171e28c823b10.pptx','previews/e288713cd934b20229ef4f28eed75d64a2c476808ced52b54f0171e28c823b10.pdf',90467294,'e288713cd934b20229ef4f28eed75d64a2c476808ced52b54f0171e28c823b10',1,'',20
FROM biz_guide_grade g WHERE g.grade=8
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,21,'文物保护新手段','第21课 文物保护新手段_1724058380711.pptx','6f/6f5e417f69c529b3cc5596a4ad8a6b4f895f3c9f7c3d653c0e29e61d189c35e2.pptx','previews/6f5e417f69c529b3cc5596a4ad8a6b4f895f3c9f7c3d653c0e29e61d189c35e2.pdf',5024128,'6f5e417f69c529b3cc5596a4ad8a6b4f895f3c9f7c3d653c0e29e61d189c35e2',1,'',21
FROM biz_guide_grade g WHERE g.grade=8
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,22,'健康生活新设备','第22课 健康生活新设备_1724058381667.pptx','10/10d61974623b43a554ff07e9cda8baaeff9edd7d1f3ca0e7a361dea6a29bd8ca.pptx','previews/10d61974623b43a554ff07e9cda8baaeff9edd7d1f3ca0e7a361dea6a29bd8ca.pdf',32494210,'10d61974623b43a554ff07e9cda8baaeff9edd7d1f3ca0e7a361dea6a29bd8ca',1,'',22
FROM biz_guide_grade g WHERE g.grade=8
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,23,'医疗设施新功能','第23课 医疗设施新功能_1724058383296.pptx','78/78b5f79aa6eaca06f991c4e56015bccd386d7a0f88ddbfa3643188e43f35439a.pptx','previews/78b5f79aa6eaca06f991c4e56015bccd386d7a0f88ddbfa3643188e43f35439a.pdf',2652783,'78b5f79aa6eaca06f991c4e56015bccd386d7a0f88ddbfa3643188e43f35439a',1,'',23
FROM biz_guide_grade g WHERE g.grade=8
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,24,'农业生产新模式','第24课 农业生产新模式_1724058384301.pptx','25/25f16c06204ef4c167dcb10c4066fe7630b4c39eae6b8043070245d89595e619.pptx','previews/25f16c06204ef4c167dcb10c4066fe7630b4c39eae6b8043070245d89595e619.pdf',3268938,'25f16c06204ef4c167dcb10c4066fe7630b4c39eae6b8043070245d89595e619',1,'',24
FROM biz_guide_grade g WHERE g.grade=8
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,25,'学习探究新工具','第25课 学习探究新工具.pptx','a6/a6f8704b46b26d1fe5aa05d7fff48d0b8c76ce9c1108638e449833c75e6b5a81.pptx','previews/a6f8704b46b26d1fe5aa05d7fff48d0b8c76ce9c1108638e449833c75e6b5a81.pdf',3323100,'a6f8704b46b26d1fe5aa05d7fff48d0b8c76ce9c1108638e449833c75e6b5a81',1,'',25
FROM biz_guide_grade g WHERE g.grade=8
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,26,'数字世界有身份','第26课 数字世界有身份.pptx','5b/5be13edcfc6dbed73ce172a288cdeed5ad2051016ce8e9656ff7cbbda6460c73.pptx','previews/5be13edcfc6dbed73ce172a288cdeed5ad2051016ce8e9656ff7cbbda6460c73.pdf',6001947,'5be13edcfc6dbed73ce172a288cdeed5ad2051016ce8e9656ff7cbbda6460c73',1,'',26
FROM biz_guide_grade g WHERE g.grade=8
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,27,'安全事件与风险','第27课 安全事件与风险_1724058385254.pptx','7a/7ae361918a18eacefce057639a8056a18dffee2022f15b6e24a6526fa55c00a2.pptx','previews/7ae361918a18eacefce057639a8056a18dffee2022f15b6e24a6526fa55c00a2.pdf',9425775,'7ae361918a18eacefce057639a8056a18dffee2022f15b6e24a6526fa55c00a2',1,'',27
FROM biz_guide_grade g WHERE g.grade=8
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,28,'安全防范讲策略','第28课 安全防范讲策略.pptx','69/693886028d2b3531c7639ae2c5657e2c306e82e64be7d7768deab142ae9b6f7c.pptx','previews/693886028d2b3531c7639ae2c5657e2c306e82e64be7d7768deab142ae9b6f7c.pdf',4341965,'693886028d2b3531c7639ae2c5657e2c306e82e64be7d7768deab142ae9b6f7c',1,'',28
FROM biz_guide_grade g WHERE g.grade=8
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,29,'自主可控意义大','第29课 自主可控意义大.pptx','4f/4fbc2150816e84fb58e092e6aa8b5b68af0d346014b5fa52f469a6f9510db9b8.pptx','previews/4fbc2150816e84fb58e092e6aa8b5b68af0d346014b5fa52f469a6f9510db9b8.pdf',4847663,'4fbc2150816e84fb58e092e6aa8b5b68af0d346014b5fa52f469a6f9510db9b8',1,'',29
FROM biz_guide_grade g WHERE g.grade=8
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);
INSERT INTO biz_guide_lesson (guide_id,lesson_no,title,filename,original_path,preview_path,bytes,sha256,available,note,sort_no)
SELECT g.guide_id,30,'系统安全需升级','第30课 系统安全需升级_1724058386471.pptx','27/2756c274b6cf15312afdf67a6da104b4497661edd3cf2cf546789b229ee92463.pptx','previews/2756c274b6cf15312afdf67a6da104b4497661edd3cf2cf546789b229ee92463.pdf',2058447,'2756c274b6cf15312afdf67a6da104b4497661edd3cf2cf546789b229ee92463',1,'',30
FROM biz_guide_grade g WHERE g.grade=8
ON DUPLICATE KEY UPDATE title=VALUES(title),filename=VALUES(filename),original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),sha256=VALUES(sha256),available=VALUES(available),note=VALUES(note),sort_no=VALUES(sort_no);

COMMIT;

-- ------------------------- 导入后复核 -------------------------
SELECT origin,COUNT(*) AS cases FROM biz_textbook_case WHERE status='PUBLISHED' GROUP BY origin;
SELECT COUNT(*) AS book_resources FROM biz_textbook_book_resource;
SELECT g.grade,COUNT(l.lesson_id) AS lessons,SUM(l.available=1) AS available,SUM(l.available=0) AS missing
FROM biz_guide_grade g LEFT JOIN biz_guide_lesson l ON l.guide_id=g.guide_id GROUP BY g.grade ORDER BY g.grade;
SELECT COUNT(*) AS duplicate_case_resources FROM (
  SELECT case_id,resource_key FROM biz_textbook_case_resource GROUP BY case_id,resource_key HAVING COUNT(*)>1
) t;
