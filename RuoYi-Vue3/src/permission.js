import router from "./router";
import { ElMessage } from "element-plus";
import NProgress from "nprogress";
import "nprogress/nprogress.css";
import { getToken } from "@/utils/auth";
import { isHttp, isPathMatch } from "@/utils/validate";
import { isRelogin } from "@/utils/request";
import useUserStore from "@/store/modules/user";
import useSettingsStore from "@/store/modules/settings";
import usePermissionStore from "@/store/modules/permission";

NProgress.configure({ showSpinner: false });

// 学生首页必须登录；未登录访问 /student/index 应进入登录流程，不能免鉴权直入
const whiteList = ["/login", "/register"];

const isWhiteList = (path) => {
  return whiteList.some((pattern) => isPathMatch(pattern, path));
};

router.beforeEach((to, from, next) => {
  NProgress.start();
  // 平台菜单实际路由为 /platform-update，兼容历史书签中的旧地址。
  if (to.path === '/business/platform-update') {
    next({ path: '/platform-update', replace: true });
    return;
  }
  if (getToken()) {
    to.meta.title && useSettingsStore().setTitle(to.meta.title);
    /* has token*/
    if (to.path === "/login") {
      next({ path: "/" });
      NProgress.done();
    } else {
      const userStore = useUserStore();
      if (userStore.roles.length === 0) {
        isRelogin.show = true;
        // 判断当前用户是否已拉取完user_info信息
        userStore
          .getInfo()
          .then(() => {
            isRelogin.show = false;
            const roles = userStore.roles;

            usePermissionStore()
              .generateRoutes(roles)
              .then((accessRoutes) => {
                accessRoutes.forEach((route) => {
                  if (!isHttp(route.path)) {
                    router.addRoute(route);
                  }
                });
                
                const isStudent = roles.includes("student");
                const isTeacher = roles.includes("teacher");
                const isResearcher = roles.includes("researcher");

                if (isStudent) {
                  // 学生登录后或刷新，强制跳转到学生首页 (除非已经在路径下)
                  if (!to.path.startsWith("/student")) {
                    next({ path: "/student/index", replace: true });
                  } else {
                    next({ ...to, replace: true });
                  }
                } else {
                  // 教师/教研员/管理员 登录后跳转逻辑
                  if (isTeacher && userStore.needChangePwd && to.path !== '/user/profile') {
                    ElMessage.warning('为保证账号安全，请先修改为强密码（含大小写字母、数字及特殊符号，至少6位）');
                    next({ path: '/user/profile', replace: true });
                  } else if (to.path === "/" || to.path === "/index") {
                    if (isResearcher) {
                      // 教研员登录后默认进入平台概览，避免落到系统管理类页面。
                      next({ path: "/platform/overview", replace: true });
                    } else if (isTeacher) {
                      next({ path: "/teacher-dashboard", replace: true });
                    } else {
                      next({ ...to, replace: true });
                    }
                  } else {
                    next({ ...to, replace: true });
                  }
                }
              });
          })
          .catch((err) => {
            userStore.logOut().then(() => {
              ElMessage.error(err);
              next({ path: "/" });
            });
          });
      } else {
        // 已有roles信息，处理刷新或直接访问URL的情况
        const isStudent = userStore.roles.includes("student");
        const isTeacher = userStore.roles.includes("teacher");
        const isResearcher = userStore.roles.includes("researcher");

        if (isStudent && !to.path.startsWith("/student")) {
          // 如果是学生，但目标路径不是以/student开头，则强制送回学生首页
          next({ path: "/student/index" });
        } else if (isTeacher && userStore.needChangePwd && to.path !== '/user/profile') {
          ElMessage.warning('为保证账号安全，请先修改为强密码（含大小写字母、数字及特殊符号，至少6位）');
          next({ path: '/user/profile' });
        } else if (isResearcher && (to.path === "/" || to.path === "/index")) {
          // 如果是教研员，访问首页时强制跳转到平台概览
          next({ path: "/platform/overview" });
        } else if (isTeacher && (to.path === "/" || to.path === "/index")) {
          // 如果是教师，访问首页时强制跳转到教师工作台
          next({ path: "/teacher-dashboard" });
        } else {
          // 其他情况直接放行
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
});

router.afterEach(() => {
  NProgress.done();
});
