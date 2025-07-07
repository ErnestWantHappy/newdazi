import router from './router'
import { ElMessage } from 'element-plus'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken } from '@/utils/auth'
import { isHttp, isPathMatch } from '@/utils/validate'
import { isRelogin } from '@/utils/request'
import useUserStore from '@/store/modules/user'
import useSettingsStore from '@/store/modules/settings'
import usePermissionStore from '@/store/modules/permission'

NProgress.configure({ showSpinner: false });

const whiteList = ['/login', '/register', '/student/index'];

const isWhiteList = (path) => {
  return whiteList.some(pattern => isPathMatch(pattern, path));
}

router.beforeEach((to, from, next) => {
  NProgress.start();
  if (getToken()) {
    to.meta.title && useSettingsStore().setTitle(to.meta.title);
    /* has token*/
    if (to.path === '/login') {
      next({ path: '/' });
      NProgress.done();
    } else {
      const userStore = useUserStore();
      if (userStore.roles.length === 0) {
        isRelogin.show = true;
        // 判断当前用户是否已拉取完user_info信息
        userStore.getInfo().then(() => {
          isRelogin.show = false;
          const roles = userStore.roles;
          const isStudent = roles.includes('student');

          if (isStudent) {
            // 如果是学生，直接放行到学生首页
            next({ path: '/student/index', replace: true });
          } else {
            // 非学生角色，正常生成动态路由
            usePermissionStore().generateRoutes().then(accessRoutes => {
              accessRoutes.forEach(route => {
                if (!isHttp(route.path)) {
                  router.addRoute(route);
                }
              })
              next({ ...to, replace: true });
            })
          }
        }).catch(err => {
          userStore.logOut().then(() => {
            ElMessage.error(err);
            next({ path: '/' });
          })
        })
      } else {
        // 已有roles信息，处理刷新或直接访问URL的情况
        const isStudent = userStore.roles.includes('student');
        if (isStudent && !to.path.startsWith('/student')) {
          // 如果是学生，但目标路径不是以/student开头，则强制送回学生首页
          next({ path: '/student/index' });
        } else {
          // 如果是学生访问学生路径，或非学生访问任何路径，则直接放行
          next();
        }
      }
    }
  } else {
    // 没有token
    if (isWhiteList(to.path)) {
      // 在免登录白名单，直接进入
      next();
    } else {
      next(`/login?redirect=${to.fullPath}`); // 否则全部重定向到登录页
      NProgress.done();
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})