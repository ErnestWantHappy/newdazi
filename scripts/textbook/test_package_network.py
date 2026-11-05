"""资源条件的联网识别回归，不访问外网或调用共享配置。"""
import unittest
from consolidate_case_packages import check_network


class PackageNetworkTest(unittest.TestCase):
    def test_cdn_url_is_not_a_comment(self):
        self.assertTrue(check_network('demo.html', '<script src="https://cdn.example.com/library.js"></script>'))

    def test_model_request_requires_network(self):
        self.assertTrue(check_network('demo.html', 'fetch("https://api.example.com/v1/chat")'))

    def test_local_and_namespace_are_not_external_dependencies(self):
        self.assertFalse(check_network('demo.html', '<svg xmlns="http://www.w3.org/2000/svg"></svg>'))
        self.assertFalse(check_network('demo.html', 'fetch("http://localhost:8000/api")'))
        self.assertFalse(check_network('demo.html', '<!-- <script src="https://cdn.example.com/x.js"></script> -->'))


if __name__ == '__main__':
    unittest.main()
