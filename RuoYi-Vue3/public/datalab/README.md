# 数据分析实验运行资产

- openpyxl 3.1.5、et_xmlfile 2.0.0：从 PyPI 官方发布下载的纯 Python wheel，许可证保留在 wheel 内的 dist-info 目录。
- NotoSansSC.ttf：Google Fonts 的 Noto Sans SC 可变中文字体，许可证见 OFL.txt。
- openpyxl SHA256：5282c12b107bffeef825f4617dc029afaf41d0ea60823bbb665ef3079dc79de2
- et_xmlfile SHA256：7a91720bc756843502c3b7504c77b8fe44217c85c537d85037f0f536151b2caa

这些文件随前端部署，不能只复制 JS/CSS。Python 运行器先加载 micropip，再安装上述固定 wheel，不在学生运行时从 PyPI 解析依赖。

Pyodide 0.26.4 默认从 jsDelivr 加载。学校离线部署时，将该版本 full 目录及 pandas/matplotlib/numpy/micropip 依赖文件镜像到静态站点，并在构建时配置 VITE_PYODIDE_BASE_URL。VITE_PYODIDE_FONT_URL 可覆盖字体地址；默认使用本站 /datalab/NotoSansSC.ttf。
