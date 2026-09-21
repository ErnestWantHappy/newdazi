import { test } from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import vm from 'node:vm';

// 直接执行页面函数，使用可控延迟模拟真实请求交错，避免测试一份复制的实现。
const source = fs.readFileSync(new URL('../grading.vue', import.meta.url), 'utf8');
function extract(start, end) {
    return source.slice(source.indexOf(start), source.indexOf(end, source.indexOf(start)));
}
function setup() {
    const row = id => ({ studentId: id, answerId: id + 10, studentNo: id,
        submitted: true, score: null, practicalVersionId: id, maxScore: 10 });
    const a = row(1), b = row(2);
    const requests = [], grades = [], messages = [];
    const ref = value => ({ value });
    const ctx = {
        selectedLessonId: ref(1), selectedQuestionId: ref(1), selectedClassCode: ref('1'),
        classes: ref([{ classCode: '1', entryYear: '2024' }]),
        currentStudent: ref(a), currentIndex: ref(0), submissions: ref([a, b]),
        submissionsRequestSeq: 0, loading: ref(false), submitting: ref(false),
        gradingSnapshotStale: ref(false), currentScore: ref(8), previewUrl: ref('A'),
        deadlineStatus: ref({ canGrade: true }), useItemScoring: ref(false),
        gradingInputMode: ref('NUMERIC'), overallStarCount: ref(0),
        getPracticalSubmissions: () => new Promise(resolve => requests.push(resolve)),
        gradeSubmission: data => new Promise((resolve, reject) => grades.push({ data, resolve, reject })),
        restoreLatestAiJob() {}, syncClassroomSocket() {}, loadDeadlineStatus() {},
        refreshSelectedAttachmentPreview() { ctx.previewUrl.value = String(ctx.currentStudent.value.studentId); },
        selectStudent(student, index) {
            ctx.currentStudent.value = student; ctx.currentIndex.value = index;
            ctx.currentScore.value = student.score; ctx.gradingSnapshotStale.value = false;
            ctx.refreshSelectedAttachmentPreview();
        },
        ElMessage: Object.fromEntries(['error', 'warning', 'success', 'info'].map(k => [k, m => messages.push(m)])),
        questions: ref([{ questionId: 1 }]), isFullscreen: ref(false), autoFocusItem() {}
    };
    vm.createContext(ctx);
    vm.runInContext(extract('function loadSubmissions(options = {})', 'const gradedCount'), ctx);
    vm.runInContext(extract('async function submitScore()', 'async function returnCurrentTask()'), ctx);
    vm.runInContext(extract('async function nextSubmittedStudent()', 'function prevStudent()'), ctx);
    return { ctx, a, b, requests, grades, messages };
}

test('迟到刷新保持当前学生、输入和对应预览', async () => {
    const { ctx, a, b, requests } = setup();
    const pending = ctx.loadSubmissions({ silent: true });
    ctx.selectStudent(b, 1); ctx.currentScore.value = 6;
    requests[0]({ data: [{ ...a }, { ...b }] }); await pending;
    assert.equal(ctx.currentStudent.value.studentId, 2);
    assert.equal(ctx.currentScore.value, 6);
    assert.equal(ctx.previewUrl.value, '2');
});

test('提交废弃旧请求，保存后切到下一位且成绩不回退', async () => {
    const { ctx, a, b, requests, grades } = setup();
    const pending = ctx.loadSubmissions({ silent: true });
    const saving = ctx.submitScore();
    assert.equal(grades.length, 1);
    assert.equal(ctx.loadSubmissions({ silent: true }), undefined);
    grades[0].resolve(); await saving;
    requests[0]({ data: [{ ...a, score: null }, { ...b }] }); await pending;
    assert.equal(ctx.currentStudent.value.studentId, 2);
    assert.equal(ctx.submissions.value[0].score, 8);
    assert.equal(requests.length, 2);
});

for (const changed of [{ score: 5 }, { practicalVersionId: 99 }, { answerId: 99 }, { studentAnswer: 'new' }]) {
    test(`外部变化保留评分快照并阻止提交 ${JSON.stringify(changed)}`, async () => {
        const { ctx, a, b, requests, grades } = setup();
        const pending = ctx.loadSubmissions({ silent: true });
        requests[0]({ data: [{ ...a, ...changed }, b] }); await pending;
        assert.equal(ctx.gradingSnapshotStale.value, true);
        assert.equal(ctx.currentStudent.value, a);
        assert.equal(ctx.currentScore.value, 8);
        await ctx.submitScore(); assert.equal(grades.length, 0);
        const reload = ctx.reloadCurrentSubmission();
        requests[1]({ data: [{ ...a, ...changed }, b] }); await reload;
        assert.equal(ctx.gradingSnapshotStale.value, false);
        for (const key of Object.keys(changed)) assert.equal(ctx.currentStudent.value[key], changed[key]);
    });
}

test('保存失败不跳转，重复点击只发一次保存', async () => {
    const { ctx, grades } = setup();
    const saving = ctx.submitScore(); await ctx.submitScore();
    assert.equal(grades.length, 1);
    grades[0].reject(new Error('conflict')); await saving;
    assert.equal(ctx.currentStudent.value.studentId, 1);
    assert.equal(ctx.submitting.value, false);
});

test('连续两人提交保留两人成绩并明确批改结束', async () => {
    const { ctx, grades, requests, messages, a, b } = setup();
    const first = ctx.submitScore(); grades[0].resolve(); await first;
    ctx.currentScore.value = 7;
    const second = ctx.submitScore(); grades[1].resolve(); await second;
    requests[0]({ data: [{ ...a, score: 8 }, { ...b, score: null }] });
    await Promise.resolve();
    assert.equal(ctx.submissions.value[0].score, 8);
    assert.equal(ctx.submissions.value[1].score, 7);
    assert.equal(ctx.currentStudent.value.studentId, 2);
    assert.ok(messages.some(m => m.includes('本题已批改完成')));
});

test('名单加载中不允许提交，静默轮询不能抢占主动重载', async () => {
    const { ctx, requests, grades } = setup();
    ctx.loadSubmissions();
    ctx.loadSubmissions({ silent: true }); await ctx.submitScore();
    assert.equal(requests.length, 1); assert.equal(grades.length, 0);
});
