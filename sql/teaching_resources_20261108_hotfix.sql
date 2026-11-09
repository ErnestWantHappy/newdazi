-- 教学资料修复 1.30.77：先备份及上传校验；按稳定来源键定位，不使用本机ID。
SET NAMES utf8mb4;
START TRANSACTION;
INSERT INTO biz_textbook_case(source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2023reffe648e16dd28d17f931ddb4f67e9a130',b.book_id,'lesson-1','在线学习（2023版参考）','叶晓静','象山县丹山书院','NINGBO2023','PUBLISHED',301
FROM biz_textbook_book b JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-1' AND s.kind='LESSON'
WHERE b.book_code='xxkj-8-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin=VALUES(origin);
INSERT INTO biz_textbook_case_resource(case_id,resource_key,category,title,filename,original_path,preview_path,bytes,description,requirements,sort_no)
SELECT case_id,'nb2023reffe648e16dd28d17f931ddb4f67e9a130-design','DESIGN','在线学习（2023版参考）','2023参考-八上-第01课-在线学习.docx','41/41d6c7afc35a839bd155452681d9172cd68cac807f4385498556c6c88b744689.docx','a3/a3bfae29e991f55a4899cd39184fef14d6e731d1b7569b2d1a4cc24ace58bc4b.pdf',2573219,'宁波版 2023 参考；按教学主题对应现行第1课；旧版课号及表述可能不同。原始来源：2023版/2023新教材教学设计（宁波版）word/2023义务教育信息科技八年级上册教学设计.docx','',1
FROM biz_textbook_case WHERE source_key='nb2023reffe648e16dd28d17f931ddb4f67e9a130'
ON DUPLICATE KEY UPDATE original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),description=VALUES(description);
INSERT INTO biz_textbook_case(source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2023ref4a83f32f523dc82faf7f7494f09ce80a',b.book_id,'lesson-2','电子支付（2023版参考）','黄芳芳','象山县丹城中学','NINGBO2023','PUBLISHED',301
FROM biz_textbook_book b JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-2' AND s.kind='LESSON'
WHERE b.book_code='xxkj-8-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin=VALUES(origin);
INSERT INTO biz_textbook_case_resource(case_id,resource_key,category,title,filename,original_path,preview_path,bytes,description,requirements,sort_no)
SELECT case_id,'nb2023ref4a83f32f523dc82faf7f7494f09ce80a-design','DESIGN','电子支付（2023版参考）','2023参考-八上-第02课-电子支付.docx','14/14add340ddda01f28d2e633caacfb041474222288052a09244ce2798b20d322e.docx','a1/a11185bfc66aca7686882d085a08b4138ab608e06cd6d009fabb880382682bbc.pdf',2567053,'宁波版 2023 参考；按教学主题对应现行第2课；旧版课号及表述可能不同。原始来源：2023版/2023新教材教学设计（宁波版）word/2023义务教育信息科技八年级上册教学设计.docx','',1
FROM biz_textbook_case WHERE source_key='nb2023ref4a83f32f523dc82faf7f7494f09ce80a'
ON DUPLICATE KEY UPDATE original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),description=VALUES(description);
INSERT INTO biz_textbook_case(source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2023ref360b978504ae1349a249449509c6584d',b.book_id,'lesson-3','在线交流（2023版参考）','郑瑾','丹城中学','NINGBO2023','PUBLISHED',301
FROM biz_textbook_book b JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-3' AND s.kind='LESSON'
WHERE b.book_code='xxkj-8-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin=VALUES(origin);
INSERT INTO biz_textbook_case_resource(case_id,resource_key,category,title,filename,original_path,preview_path,bytes,description,requirements,sort_no)
SELECT case_id,'nb2023ref360b978504ae1349a249449509c6584d-design','DESIGN','在线交流（2023版参考）','2023参考-八上-第03课-在线交流.docx','c0/c019d495bc7b708a3cd41ccd8757dbf4cb88a0087b079d13bcc26e5217024c53.docx','d9/d9393bd66aa47df5c2a33f1b242b916f634bf1e156c15c6bc5dfbc8fb0633d1c.pdf',2567520,'宁波版 2023 参考；按教学主题对应现行第3课；旧版课号及表述可能不同。原始来源：2023版/2023新教材教学设计（宁波版）word/2023义务教育信息科技八年级上册教学设计.docx','',1
FROM biz_textbook_case WHERE source_key='nb2023ref360b978504ae1349a249449509c6584d'
ON DUPLICATE KEY UPDATE original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),description=VALUES(description);
INSERT INTO biz_textbook_case(source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2023ref12bee56cd98b41226b1e6e3cb21407fe',b.book_id,'lesson-4','在线协同（2023版参考）','郑琦','象山县丹城中学','NINGBO2023','PUBLISHED',301
FROM biz_textbook_book b JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-4' AND s.kind='LESSON'
WHERE b.book_code='xxkj-8-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin=VALUES(origin);
INSERT INTO biz_textbook_case_resource(case_id,resource_key,category,title,filename,original_path,preview_path,bytes,description,requirements,sort_no)
SELECT case_id,'nb2023ref12bee56cd98b41226b1e6e3cb21407fe-design','DESIGN','在线协同（2023版参考）','2023参考-八上-第04课-在线协同.docx','58/58e3a3c5373c2a47913ba9484e74127dbf00da5f920488250c866f687f1b4232.docx','8d/8d6231b81f191162a5c1fe21ef3e9e3e479f0d7e99ae75dec2b2978c6b6f538b.pdf',2571589,'宁波版 2023 参考；按教学主题对应现行第4课；旧版课号及表述可能不同。原始来源：2023版/2023新教材教学设计（宁波版）word/2023义务教育信息科技八年级上册教学设计.docx','',1
FROM biz_textbook_case WHERE source_key='nb2023ref12bee56cd98b41226b1e6e3cb21407fe'
ON DUPLICATE KEY UPDATE original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),description=VALUES(description);
INSERT INTO biz_textbook_case(source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2023ref7b26d84e59fa7e74b898ec3a148134a2',b.book_id,'lesson-5','在线应用的实践（2023版参考）','张海英','象山县石浦中学','NINGBO2023','PUBLISHED',301
FROM biz_textbook_book b JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-5' AND s.kind='LESSON'
WHERE b.book_code='xxkj-8-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin=VALUES(origin);
INSERT INTO biz_textbook_case_resource(case_id,resource_key,category,title,filename,original_path,preview_path,bytes,description,requirements,sort_no)
SELECT case_id,'nb2023ref7b26d84e59fa7e74b898ec3a148134a2-design','DESIGN','在线应用的实践（2023版参考）','2023参考-八上-第05课-在线应用的实践.docx','6b/6b0dd3b325434fb6a6928bf84f303e21bdcc60845052623c7354bf92173014c5.docx','96/96d50c90f65829df3ad2c7f7e89584517bfe2bd04c5696338cb845abe1f6a7d3.pdf',2566850,'宁波版 2023 参考；按教学主题对应现行第5课；旧版课号及表述可能不同。原始来源：2023版/2023新教材教学设计（宁波版）word/2023义务教育信息科技八年级上册教学设计.docx','',1
FROM biz_textbook_case WHERE source_key='nb2023ref7b26d84e59fa7e74b898ec3a148134a2'
ON DUPLICATE KEY UPDATE original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),description=VALUES(description);
INSERT INTO biz_textbook_case(source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2023refd0699b593ef8af21700cad6e0f410e03',b.book_id,'lesson-6','互联网应用中的数据（2023版参考）','沈芝','象山县塔山中学','NINGBO2023','PUBLISHED',301
FROM biz_textbook_book b JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-6' AND s.kind='LESSON'
WHERE b.book_code='xxkj-8-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin=VALUES(origin);
INSERT INTO biz_textbook_case_resource(case_id,resource_key,category,title,filename,original_path,preview_path,bytes,description,requirements,sort_no)
SELECT case_id,'nb2023refd0699b593ef8af21700cad6e0f410e03-design','DESIGN','互联网应用中的数据（2023版参考）','2023参考-八上-第06课-互联网应用中的数据.docx','e1/e1e194f209257359971617c5c4a25302ccb8f1de85904fb2b109605de56f8451.docx','c0/c0b1d46bbf6ead0398f00dd54a08c0e26b53a659b3d188eb0776b05dcc77ed94.pdf',2591021,'宁波版 2023 参考；按教学主题对应现行第6课；旧版课号及表述可能不同。原始来源：2023版/2023新教材教学设计（宁波版）word/2023义务教育信息科技八年级上册教学设计.docx','',1
FROM biz_textbook_case WHERE source_key='nb2023refd0699b593ef8af21700cad6e0f410e03'
ON DUPLICATE KEY UPDATE original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),description=VALUES(description);
INSERT INTO biz_textbook_case(source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2023ref491445d4448f51dd59e8c151920a550c',b.book_id,'lesson-7','网页的数据组织（2023版参考）','顾陆艇','贤庠学校','NINGBO2023','PUBLISHED',301
FROM biz_textbook_book b JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-7' AND s.kind='LESSON'
WHERE b.book_code='xxkj-8-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin=VALUES(origin);
INSERT INTO biz_textbook_case_resource(case_id,resource_key,category,title,filename,original_path,preview_path,bytes,description,requirements,sort_no)
SELECT case_id,'nb2023ref491445d4448f51dd59e8c151920a550c-design','DESIGN','网页的数据组织（2023版参考）','2023参考-八上-第07课-网页的数据组织.docx','9e/9ef6e3317fb39b9ea7b13e3dd1a666e0fbf3cbae0388b1ce3d0729411ef86e9b.docx','90/90c3539d93476feed880a8c4baef2edd804edee1245a1013c9d0167e8c1210d4.pdf',2569029,'宁波版 2023 参考；按教学主题对应现行第7课；旧版课号及表述可能不同。原始来源：2023版/2023新教材教学设计（宁波版）word/2023义务教育信息科技八年级上册教学设计.docx','',1
FROM biz_textbook_case WHERE source_key='nb2023ref491445d4448f51dd59e8c151920a550c'
ON DUPLICATE KEY UPDATE original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),description=VALUES(description);
INSERT INTO biz_textbook_case(source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2023refd940bc7491b0d86c93117605542a9faf',b.book_id,'lesson-8','网页的数据呈现（2023版参考）','吴晓燕','墙头学校','NINGBO2023','PUBLISHED',301
FROM biz_textbook_book b JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-8' AND s.kind='LESSON'
WHERE b.book_code='xxkj-8-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin=VALUES(origin);
INSERT INTO biz_textbook_case_resource(case_id,resource_key,category,title,filename,original_path,preview_path,bytes,description,requirements,sort_no)
SELECT case_id,'nb2023refd940bc7491b0d86c93117605542a9faf-design','DESIGN','网页的数据呈现（2023版参考）','2023参考-八上-第08课-网页的数据呈现.docx','0f/0f61c76122d6f4828b75f5cc67c874ae7639e81ff297bf72815ee9feba615817.docx','c9/c9cbfb326749cd7112bcf31c068ff8acad2a1a943d1cb9f134e26f889807c7d7.pdf',2574939,'宁波版 2023 参考；按教学主题对应现行第8课；旧版课号及表述可能不同。原始来源：2023版/2023新教材教学设计（宁波版）word/2023义务教育信息科技八年级上册教学设计.docx','',1
FROM biz_textbook_case WHERE source_key='nb2023refd940bc7491b0d86c93117605542a9faf'
ON DUPLICATE KEY UPDATE original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),description=VALUES(description);
INSERT INTO biz_textbook_case(source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2023refcf2d9c0e420d24033d29e2c0a00186c8',b.book_id,'lesson-9','网页的数据编码（2023版参考）','陈艳蕾','象山县实验中学','NINGBO2023','PUBLISHED',301
FROM biz_textbook_book b JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-9' AND s.kind='LESSON'
WHERE b.book_code='xxkj-8-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin=VALUES(origin);
INSERT INTO biz_textbook_case_resource(case_id,resource_key,category,title,filename,original_path,preview_path,bytes,description,requirements,sort_no)
SELECT case_id,'nb2023refcf2d9c0e420d24033d29e2c0a00186c8-design','DESIGN','网页的数据编码（2023版参考）','2023参考-八上-第09课-网页的数据编码.docx','66/66c167e01db7531cd9f9dd644fa27adeb7e598b2b647b121925be5eb1b78c2ce.docx','4c/4c66cf94b9f5a36a951a92e17fa64b25353adba997d3de8b828579acc0083f2f.pdf',2570935,'宁波版 2023 参考；按教学主题对应现行第9课；旧版课号及表述可能不同。原始来源：2023版/2023新教材教学设计（宁波版）word/2023义务教育信息科技八年级上册教学设计.docx','',1
FROM biz_textbook_case WHERE source_key='nb2023refcf2d9c0e420d24033d29e2c0a00186c8'
ON DUPLICATE KEY UPDATE original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),description=VALUES(description);
INSERT INTO biz_textbook_case(source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2023refbdae47d47d932c510c32a6aec38ca21e',b.book_id,'lesson-10','网页的编辑与发布（2023版参考）','郑月阳','鹤浦中学','NINGBO2023','PUBLISHED',301
FROM biz_textbook_book b JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-10' AND s.kind='LESSON'
WHERE b.book_code='xxkj-8-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin=VALUES(origin);
INSERT INTO biz_textbook_case_resource(case_id,resource_key,category,title,filename,original_path,preview_path,bytes,description,requirements,sort_no)
SELECT case_id,'nb2023refbdae47d47d932c510c32a6aec38ca21e-design','DESIGN','网页的编辑与发布（2023版参考）','2023参考-八上-第10课-网页的编辑与发布.docx','d4/d49e833dc060f1925f7f8d325fe0da232c69f5fce7e4ec6534fd03c57892fd85.docx','f5/f54f1c8f39117d77d082d65ca1912b525692bb57af8260e5f9e50be25e29a3e9.pdf',2567788,'宁波版 2023 参考；按教学主题对应现行第10课；旧版课号及表述可能不同。原始来源：2023版/2023新教材教学设计（宁波版）word/2023义务教育信息科技八年级上册教学设计.docx','',1
FROM biz_textbook_case WHERE source_key='nb2023refbdae47d47d932c510c32a6aec38ca21e'
ON DUPLICATE KEY UPDATE original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),description=VALUES(description);
INSERT INTO biz_textbook_case(source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2023ref868845ff951099d5b6bd59aba501f319',b.book_id,'lesson-11','数据备份与加密（2023版参考）','胡旖旎','象山县茅洋乡茅洋学校','NINGBO2023','PUBLISHED',301
FROM biz_textbook_book b JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-11' AND s.kind='LESSON'
WHERE b.book_code='xxkj-8-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin=VALUES(origin);
INSERT INTO biz_textbook_case_resource(case_id,resource_key,category,title,filename,original_path,preview_path,bytes,description,requirements,sort_no)
SELECT case_id,'nb2023ref868845ff951099d5b6bd59aba501f319-design','DESIGN','数据备份与加密（2023版参考）','2023参考-八上-第11课-数据备份与加密.docx','75/7581d911b072a9b363703f405255fb1df492d61fdd48e60a25d51b888369d674.docx','8f/8f0672a467c819d617cec074206fe5e0baecad31508f93bca85899f141af5d9c.pdf',2566104,'宁波版 2023 参考；按教学主题对应现行第11课；旧版课号及表述可能不同。原始来源：2023版/2023新教材教学设计（宁波版）word/2023义务教育信息科技八年级上册教学设计.docx','',1
FROM biz_textbook_case WHERE source_key='nb2023ref868845ff951099d5b6bd59aba501f319'
ON DUPLICATE KEY UPDATE original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),description=VALUES(description);
INSERT INTO biz_textbook_case(source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2023ref864139ecd8d30d081a5e24c7d8855664',b.book_id,'lesson-12','数据解密（2023版参考）','袁夏亚','象山新桥学校','NINGBO2023','PUBLISHED',301
FROM biz_textbook_book b JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-12' AND s.kind='LESSON'
WHERE b.book_code='xxkj-8-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin=VALUES(origin);
INSERT INTO biz_textbook_case_resource(case_id,resource_key,category,title,filename,original_path,preview_path,bytes,description,requirements,sort_no)
SELECT case_id,'nb2023ref864139ecd8d30d081a5e24c7d8855664-design','DESIGN','数据解密（2023版参考）','2023参考-八上-第12课-数据解密.docx','11/11d70e3ce2c30ea2d133adbe670fea8e22165f1fc3bc7af1778accfa4d077551.docx','01/0110098c236b1e59771d835da2f32e7efaf11b28dc4f6e2ed941f45acff2d601.pdf',2575882,'宁波版 2023 参考；按教学主题对应现行第12课；旧版课号及表述可能不同。原始来源：2023版/2023新教材教学设计（宁波版）word/2023义务教育信息科技八年级上册教学设计.docx','',1
FROM biz_textbook_case WHERE source_key='nb2023ref864139ecd8d30d081a5e24c7d8855664'
ON DUPLICATE KEY UPDATE original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),description=VALUES(description);
INSERT INTO biz_textbook_case(source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2023ref61d6ce903987394b387329ca29fb9e2b',b.book_id,'lesson-13','网络安全防范（2023版参考）','张惠丽','象山县启力学校','NINGBO2023','PUBLISHED',301
FROM biz_textbook_book b JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-13' AND s.kind='LESSON'
WHERE b.book_code='xxkj-8-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin=VALUES(origin);
INSERT INTO biz_textbook_case_resource(case_id,resource_key,category,title,filename,original_path,preview_path,bytes,description,requirements,sort_no)
SELECT case_id,'nb2023ref61d6ce903987394b387329ca29fb9e2b-design','DESIGN','网络安全防范（2023版参考）','2023参考-八上-第13课-网络安全防范.docx','11/1155b21116bca69204fbd04afbf11be9269e8b69ca9e73f7a1a7de3294d37fb7.docx','49/491a741c6e737f7450dc9219de3c9009ccc7eaba23b9c2ac0f2b840c8dc8119b.pdf',2567395,'宁波版 2023 参考；按教学主题对应现行第13课；旧版课号及表述可能不同。原始来源：2023版/2023新教材教学设计（宁波版）word/2023义务教育信息科技八年级上册教学设计.docx','',1
FROM biz_textbook_case WHERE source_key='nb2023ref61d6ce903987394b387329ca29fb9e2b'
ON DUPLICATE KEY UPDATE original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),description=VALUES(description);
INSERT INTO biz_textbook_case(source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2023ref1bb9ac04069a041646370923d6786aea',b.book_id,'lesson-14','网络身份认证（2023版参考）','王雪婷','林海学校','NINGBO2023','PUBLISHED',301
FROM biz_textbook_book b JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-14' AND s.kind='LESSON'
WHERE b.book_code='xxkj-8-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin=VALUES(origin);
INSERT INTO biz_textbook_case_resource(case_id,resource_key,category,title,filename,original_path,preview_path,bytes,description,requirements,sort_no)
SELECT case_id,'nb2023ref1bb9ac04069a041646370923d6786aea-design','DESIGN','网络身份认证（2023版参考）','2023参考-八上-第14课-网络身份认证.docx','89/895197e1decc9752c096c41a0e0eb74a58ff872fcffeec52812a5ee36d9007e6.docx','8a/8a9d8f04de385cc4e9b9ae1a2f60ec58e5c2e07923dfd0cdddcbb2e45b544dc3.pdf',2566135,'宁波版 2023 参考；按教学主题对应现行第14课；旧版课号及表述可能不同。原始来源：2023版/2023新教材教学设计（宁波版）word/2023义务教育信息科技八年级上册教学设计.docx','',1
FROM biz_textbook_case WHERE source_key='nb2023ref1bb9ac04069a041646370923d6786aea'
ON DUPLICATE KEY UPDATE original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),description=VALUES(description);
INSERT INTO biz_textbook_case(source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2023ref3125c80ed13462393d5ac827695c7bc7',b.book_id,'lesson-15','个人数据安全宣传（2023版参考）','程丹莉','象山县启力学校','NINGBO2023','PUBLISHED',301
FROM biz_textbook_book b JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-15' AND s.kind='LESSON'
WHERE b.book_code='xxkj-8-0-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin=VALUES(origin);
INSERT INTO biz_textbook_case_resource(case_id,resource_key,category,title,filename,original_path,preview_path,bytes,description,requirements,sort_no)
SELECT case_id,'nb2023ref3125c80ed13462393d5ac827695c7bc7-design','DESIGN','个人数据安全宣传（2023版参考）','2023参考-八上-第15课-个人数据安全宣传.docx','e1/e13c93ea61569cd841f043607449f07d79f8d12837c770b10050467de832ecab.docx','79/7974d86a2b5816b2fa3ecb848b440cb0d4170ca8b28ef65f038255cb60505b4a.pdf',2824129,'宁波版 2023 参考；按教学主题对应现行第15课；旧版课号及表述可能不同。原始来源：2023版/2023新教材教学设计（宁波版）word/2023义务教育信息科技八年级上册教学设计.docx','',1
FROM biz_textbook_case WHERE source_key='nb2023ref3125c80ed13462393d5ac827695c7bc7'
ON DUPLICATE KEY UPDATE original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),description=VALUES(description);
INSERT INTO biz_textbook_case(source_key,book_id,section_key,title,author,school,origin,status,sort_no)
SELECT 'nb2023refbde1170ba50bb8198e583aad8ddcbf96',b.book_id,'lesson-6','认识机器学习（2023版参考）','梅杨慧','宁海县跃龙中学','NINGBO2023','PUBLISHED',301
FROM biz_textbook_book b JOIN biz_textbook_section s ON s.book_id=b.book_id AND s.section_key='lesson-6' AND s.kind='LESSON'
WHERE b.book_code='xxkj-8-1-2026'
ON DUPLICATE KEY UPDATE title=VALUES(title),author=VALUES(author),school=VALUES(school),origin=VALUES(origin);
INSERT INTO biz_textbook_case_resource(case_id,resource_key,category,title,filename,original_path,preview_path,bytes,description,requirements,sort_no)
SELECT case_id,'nb2023refbde1170ba50bb8198e583aad8ddcbf96-design','DESIGN','认识机器学习（2023版参考）','2023参考-八下-第06课-认识机器学习.docx','54/547f3d891c5a3f20ea8c857a72cbaa5809901c4b4699068665bf5c33bb560090.docx','ff/ff01ce706f45b482e71097a57f9b252518ced20d505549a1c130c4e5c89d26ae.pdf',27522,'宁波版 2023 参考；按教学主题对应现行第6课；旧版课号及表述可能不同。原始来源：八下（宁海）/原教材/《机器学习》宁海县跃龙中学梅杨慧.docx','',1
FROM biz_textbook_case WHERE source_key='nb2023refbde1170ba50bb8198e583aad8ddcbf96'
ON DUPLICATE KEY UPDATE original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes),description=VALUES(description);
UPDATE biz_textbook_case_resource r JOIN biz_textbook_case c ON c.case_id=r.case_id
SET r.original_path='2e/2eed94a7116e2e7fa73c85ef4f6b07904239ae0df7328c529022d21f8769c9d4.zip',r.bytes=22965,
r.description=CONCAT(COALESCE(r.description,''),'；已整理：移除本地配置、运行数据库、日志、安装程序和缓存。'),
r.requirements='已移除本地运行环境与数据。解压后按原项目说明安装依赖，建立自己的本地数据库与配置。'
WHERE c.source_key='1d48c49c851f04fe2f1917ebd8dabadd' AND r.resource_key='pkg-e5bc4b8937b1a399c65b6f89ca11a24bd407af4956a2231e15f4ee5493f102e3' AND r.original_path='e5/e5bc4b8937b1a399c65b6f89ca11a24bd407af4956a2231e15f4ee5493f102e3.zip';
UPDATE biz_textbook_case_resource r JOIN biz_textbook_case c ON c.case_id=r.case_id
SET r.original_path='26/265370b2f216711b28e1c6c360137d30f69deab03d1036ea99604155df6115ec.zip',r.bytes=6002726,
r.description=CONCAT(COALESCE(r.description,''),'；已整理：移除本地配置、运行数据库、日志、安装程序和缓存。'),
r.requirements='已移除本地运行环境与数据。解压后按原项目说明安装依赖，建立自己的本地数据库与配置。'
WHERE c.source_key='6b0f148ae1b7d79ea2749fa09d1f16c4' AND r.resource_key='pkg-e113bb2638ede812a64e0f9ab42ae8d63c233a98f5c6fe37b3593a070d3a6d15' AND r.original_path='e1/e113bb2638ede812a64e0f9ab42ae8d63c233a98f5c6fe37b3593a070d3a6d15.zip';
UPDATE biz_textbook_case_resource r JOIN biz_textbook_case c ON c.case_id=r.case_id
SET r.original_path='db/dbb045b457470e95b087e079b32a8f182090c88b6865b729cf270bf2c981b3f6.zip',r.bytes=138144,
r.description=CONCAT(COALESCE(r.description,''),'；已整理：移除本地配置、运行数据库、日志、安装程序和缓存。'),
r.requirements='已移除本地运行环境与数据。解压后按原项目说明安装依赖，建立自己的本地数据库与配置。'
WHERE c.source_key='269d796e11a880afb11adc1a39f4e884' AND r.resource_key='pkg-7fc749ea0f274dacacf954ae004452bf2f38fedd1a826658a70d68adc56a2bc4' AND r.original_path='7f/7fc749ea0f274dacacf954ae004452bf2f38fedd1a826658a70d68adc56a2bc4.zip';
UPDATE biz_textbook_case_resource r JOIN biz_textbook_case c ON c.case_id=r.case_id
SET r.original_path='4a/4ab8931b4dd9df63e84ed63b49916b253528cf312c411d765aacc33c359ede00.zip',r.bytes=2006287,
r.description=CONCAT(COALESCE(r.description,''),'；已整理：移除本地配置、运行数据库、日志、安装程序和缓存。'),
r.requirements='已移除本地运行环境与数据。解压后按原项目说明安装依赖，建立自己的本地数据库与配置。'
WHERE c.source_key='nb202666cd2f4bb3e886f72ffccc7b0822cf36' AND r.resource_key='pkg-f5a36ddd67b6d4cdd81baec809e00b1fd1bb0e681cf0b99eff067e837a7bd983' AND r.original_path='f5/f5a36ddd67b6d4cdd81baec809e00b1fd1bb0e681cf0b99eff067e837a7bd983.zip';
UPDATE biz_textbook_case_resource r JOIN biz_textbook_case c ON c.case_id=r.case_id
SET r.original_path='4d/4d1298749b1753586d11bcb757c3f8aff785bd1931d1deec4a5efc85974dffe8.zip',r.bytes=169580998,
r.description=CONCAT(COALESCE(r.description,''),'；已整理：移除本地配置、运行数据库、日志、安装程序和缓存。'),
r.requirements='已移除本地运行环境与数据。解压后按原项目说明安装依赖，建立自己的本地数据库与配置。'
WHERE c.source_key='aad8a160f89eadba3a68d584a0284ecf' AND r.resource_key='pkg-4a511a68daef1fce04066be0972cfdf24ecfccf43dc97eb11c081a032c865782' AND r.original_path='4a/4a511a68daef1fce04066be0972cfdf24ecfccf43dc97eb11c081a032c865782.zip';
UPDATE biz_textbook_case_resource r JOIN biz_textbook_case c ON c.case_id=r.case_id
SET r.original_path='1b/1bce12e6dfa2f5ccd24a2fde737e551fd3c991d289690146de5de4e8caac34aa.zip',r.bytes=7991663,
r.description=CONCAT(COALESCE(r.description,''),'；已整理：移除本地配置、运行数据库、日志、安装程序和缓存。'),
r.requirements='已移除本地运行环境与数据。解压后按原项目说明安装依赖，建立自己的本地数据库与配置。'
WHERE c.source_key='b28fe6ce0d5bd19002aec26a067897ac' AND r.resource_key='pkg-dc763795d56bf425ea3f28f39c7331041877f34f1cd7ca32b0beba7e4460e8f8' AND r.original_path='dc/dc763795d56bf425ea3f28f39c7331041877f34f1cd7ca32b0beba7e4460e8f8.zip';
UPDATE biz_textbook_case_resource r JOIN biz_textbook_case c ON c.case_id=r.case_id
SET r.original_path='32/32d60d916c536169dacf395f223edcc973ba80121377f09dbc31c4390cf1ff0f.zip',r.bytes=57184182,
r.description=CONCAT(COALESCE(r.description,''),'；已整理：移除本地配置、运行数据库、日志、安装程序和缓存。'),
r.requirements='已移除本地运行环境与数据。解压后按原项目说明安装依赖，建立自己的本地数据库与配置。'
WHERE c.source_key='e030a29d1210eeacb137ee49572daedc' AND r.resource_key='pkg-8d45d1a3eaadadfbef026975e0dbeacb9140c69362ec018a606b59dc9f102b3c' AND r.original_path='8d/8d45d1a3eaadadfbef026975e0dbeacb9140c69362ec018a606b59dc9f102b3c.zip';
UPDATE biz_textbook_case_resource r JOIN biz_textbook_case c ON c.case_id=r.case_id
SET r.original_path='66/66f0bd7f688d446d6bd7932c172e039e6f2df10367a3ffff01d09fbcf1e0112a.zip',r.bytes=37570,
r.description=CONCAT(COALESCE(r.description,''),'；已整理：移除本地配置、运行数据库、日志、安装程序和缓存。'),
r.requirements='已移除本地运行环境与数据。解压后按原项目说明安装依赖，建立自己的本地数据库与配置。'
WHERE c.source_key='312d510591e3cb3d4451502a8b200d5d' AND r.resource_key='pkg-9ff396904aab978a91c58074750d972eb0e651fbe31df321ef626f164e594d1c' AND r.original_path='9f/9ff396904aab978a91c58074750d972eb0e651fbe31df321ef626f164e594d1c.zip';
UPDATE biz_textbook_case_resource r JOIN biz_textbook_case c ON c.case_id=r.case_id
SET r.original_path='46/462a37086623258be9923358f58cd0f261e109adfd2b6e778950126dcd682e03.zip',r.bytes=62314868,
r.description=CONCAT(COALESCE(r.description,''),'；已整理：移除本地配置、运行数据库、日志、安装程序和缓存。'),
r.requirements='已移除本地运行环境与数据。解压后按原项目说明安装依赖，建立自己的本地数据库与配置。'
WHERE c.source_key='0dd643de6210d7312321d23d24a01175' AND r.resource_key='pkg-e3d5a206a3e27a5ff1ec8c8e50dd92a8eed0856b15d2f51723aeabc1edbea2af' AND r.original_path='e3/e3d5a206a3e27a5ff1ec8c8e50dd92a8eed0856b15d2f51723aeabc1edbea2af.zip';
UPDATE biz_textbook_case_resource r JOIN biz_textbook_case c ON c.case_id=r.case_id
SET r.original_path='7f/7f42d57fcee226f9ca78439165dc6e9295305a4806c4eb68fe4024e5ac558bcd.zip',r.bytes=58881676,
r.description=CONCAT(COALESCE(r.description,''),'；已整理：移除本地配置、运行数据库、日志、安装程序和缓存。'),
r.requirements='已移除本地运行环境与数据。解压后按原项目说明安装依赖，建立自己的本地数据库与配置。'
WHERE c.source_key='fa2dab773e1e898d2102fa96a99d5605' AND r.resource_key='pkg-20694059d7dc3822be9fa08bade5e0d46ac3fb2000ba7f0a3716393b8c4ffb02' AND r.original_path='20/20694059d7dc3822be9fa08bade5e0d46ac3fb2000ba7f0a3716393b8c4ffb02.zip';
INSERT INTO biz_textbook_case_resource(case_id,resource_key,category,title,filename,original_path,preview_path,bytes,description,requirements,sort_no)
SELECT case_id,'recovered-aa55a0d7bb86dc98a6b1a4e5acc2fb0bc5d7f7c6600e9827c5267d1c31946b8f','DESIGN','寻找智能“小眼睛”（补充原件）','教学设计.pdf','aa/aa55a0d7bb86dc98a6b1a4e5acc2fb0bc5d7f7c6600e9827c5267d1c31946b8f.pdf','aa/aa55a0d7bb86dc98a6b1a4e5acc2fb0bc5d7f7c6600e9827c5267d1c31946b8f.pdf',280584,'恢复此前按同名误判重复而漏收的原件；来源：三上（前湾高新）/高新区-第3课《寻找智能“小眼睛”》(1)/教学设计/教学设计.pdf','',90
FROM biz_textbook_case WHERE source_key='3005d1f6068aae767065034b133c7ca1'
ON DUPLICATE KEY UPDATE original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes);
INSERT INTO biz_textbook_case_resource(case_id,resource_key,category,title,filename,original_path,preview_path,bytes,description,requirements,sort_no)
SELECT case_id,'recovered-014067b7704e275c57307114c42421728fe0071acb3ea88762cf10c7a5e71bd1','WORKSHEET','用神经网络进行分类（补充原件）','导学单.pdf','01/014067b7704e275c57307114c42421728fe0071acb3ea88762cf10c7a5e71bd1.pdf','01/014067b7704e275c57307114c42421728fe0071acb3ea88762cf10c7a5e71bd1.pdf',293407,'恢复此前按同名误判重复而漏收的原件；来源：五下（江北）/奉化区-第17课《利用神经网络进行分类》(1)/导学单.pdf','',90
FROM biz_textbook_case WHERE source_key='4c85b3ea4118212e2f655decb4ff2a26'
ON DUPLICATE KEY UPDATE original_path=VALUES(original_path),preview_path=VALUES(preview_path),bytes=VALUES(bytes);
UPDATE biz_textbook_book_resource SET title='教学设计汇编',description='2026版来源汇编；对应现行教材 16/18 课；覆盖第1、3、4、6、7、8、9、10、11、12、13、14、15、16、17、18课。原件按来源保留，不代表完整整册。' WHERE resource_key='book-design-xxkj-3-0-2026';
UPDATE biz_textbook_book_resource SET title='教学设计汇编',description='2026版来源汇编；对应现行教材 11/18 课；覆盖第1、2、3、4、5、6、7、8、9、10、11课。原件按来源保留，不代表完整整册。' WHERE resource_key='book-design-xxkj-3-1-2026';
UPDATE biz_textbook_book_resource SET title='教学设计汇编',description='2026版来源汇编；对应现行教材 18/18 课；覆盖第1、2、3、4、5、6、7、8、9、10、11、12、13、14、15、16、17、18课。原件按来源保留，不代表完整整册。' WHERE resource_key='book-design-xxkj-4-0-2026';
UPDATE biz_textbook_book_resource SET title='教学设计汇编',description='2026版来源汇编；对应现行教材 18/18 课；覆盖第1、2、3、4、5、6、7、8、9、10、11、12、13、14、15、16、17、18课。原件按来源保留，不代表完整整册。' WHERE resource_key='book-design-xxkj-4-1-2026';
UPDATE biz_textbook_book_resource SET title='教学设计汇编',description='2026版来源汇编；对应现行教材 16/18 课；覆盖第1、2、3、4、5、6、7、8、9、11、12、13、14、15、16、17课。原件按来源保留，不代表完整整册。' WHERE resource_key='book-design-xxkj-5-0-2026';
UPDATE biz_textbook_book_resource SET title='教学设计汇编',description='2026版来源汇编；对应现行教材 18/18 课；覆盖第1、2、3、4、5、6、7、8、9、10、11、12、13、14、15、16、17、18课。原件按来源保留，不代表完整整册。' WHERE resource_key='book-design-xxkj-5-1-2026';
UPDATE biz_textbook_book_resource SET title='教学设计汇编',description='2026版来源汇编；对应现行教材 17/18 课；覆盖第1、2、3、4、5、6、7、8、9、10、11、12、14、15、16、17、18课。原件按来源保留，不代表完整整册。' WHERE resource_key='book-design-xxkj-6-0-2026';
UPDATE biz_textbook_book_resource SET title='教学设计汇编',description='2026版来源汇编；对应现行教材 17/18 课；覆盖第1、2、3、4、5、6、7、8、9、10、11、12、13、14、15、16、18课。原件按来源保留，不代表完整整册。' WHERE resource_key='book-design-xxkj-6-1-2026';
UPDATE biz_textbook_book_resource SET title='教学设计汇编',description='2026版来源汇编；对应现行教材 20/20 课；覆盖第1、2、3、4、5、6、7、8、9、10、11、12、13、14、15、16、17、18、19、20课。原件按来源保留，不代表完整整册。' WHERE resource_key='book-design-xxkj-7-0-2026';
UPDATE biz_textbook_book_resource SET title='教学设计汇编',description='2026版来源汇编；对应现行教材 19/20 课；覆盖第1、2、3、4、5、6、7、8、9、10、12、13、14、15、16、17、18、19、20课。原件按来源保留，不代表完整整册。' WHERE resource_key='book-design-xxkj-7-1-2026';
UPDATE biz_textbook_book_resource SET title='教学设计汇编',description='2026版来源汇编；对应现行教材 4/20 课；覆盖第16、18、19、20课。原件按来源保留，不代表完整整册。' WHERE resource_key='book-design-xxkj-8-0-2026';
UPDATE biz_textbook_book_resource SET title='教学设计汇编',description='2026版来源汇编；对应现行教材 9/20 课；覆盖第1、2、3、9、16、17、18、19、20课。原件按来源保留，不代表完整整册。' WHERE resource_key='book-design-xxkj-8-1-2026';
UPDATE biz_textbook_book_resource SET title='教学设计汇编',description='2026版来源汇编；对应现行教材 1/18 课；覆盖第13课。原件按来源保留，不代表完整整册。' WHERE resource_key='book-design-xxkj-9-9-2026';
COMMIT;
