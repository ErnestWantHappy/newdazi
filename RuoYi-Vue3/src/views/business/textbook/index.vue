<template>
  <div class="app-container textbook-shelf">
    <div class="shelf-head">
      <div>
        <h2 class="shelf-title">电子教材</h2>
        <p class="shelf-sub">信息科技 2026 版 · 教师用书 · 上课翻页与打印本课</p>
      </div>
      <el-input
        v-model="keyword"
        class="shelf-search"
        placeholder="搜索年级或册次"
        clearable
        :prefix-icon="Search"
      />
    </div>

    <div v-loading="loading" class="shelf-body">
      <template v-for="group in visibleGroups" :key="group.key">
        <div v-if="group.books.length || group.pending.length" class="stage-block">
          <div class="stage-title">{{ group.title }}</div>
          <div class="book-grid">
            <div
              v-for="book in group.books"
              :key="book.bookId"
              class="book-card"
              @click="openBook(book)"
            >
              <div class="book-cover">
                <LazyPageImage
                  :book-id="book.bookId"
                  :page-no="1"
                  :alt="book.title"
                  cover
                  eager
                />
              </div>
              <div class="book-name" :title="book.title">{{ book.shortTitle }}</div>
              <div class="book-meta">{{ book.unitCount }} 单元 · {{ book.lessonCount }} 课</div>
              <div class="book-actions">
                <span class="book-open">打开阅读</span>
                <el-button
                  class="book-download"
                  link
                  type="primary"
                  size="small"
                  :loading="downloadingBookId === book.bookId"
                  :disabled="downloadingBookId !== null && downloadingBookId !== book.bookId"
                  @click.stop="downloadBook(book)"
                >
                  下载整册
                </el-button>
              </div>
            </div>

            <div v-for="item in group.pending" :key="`pending-${item.grade}-${item.semester}`" class="book-card is-pending">
              <div class="book-cover pending-cover">
                <el-icon><Document /></el-icon>
              </div>
              <div class="book-name">{{ item.shortTitle }}</div>
              <div class="book-meta">暂未上架</div>
            </div>
          </div>
        </div>
      </template>

      <el-empty v-if="!loading && !visibleGroups.length" description="没有匹配的教材" />
    </div>

    <p class="shelf-tip">
      整册 PDF 和本课 PDF 仅供教师课堂教学使用，请勿外传。
    </p>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { saveAs } from 'file-saver'
import { Document, Search } from '@element-plus/icons-vue'
import { listTextbookBooks, downloadTextbookBookPdf, TEXTBOOK_PENDING_VOLUMES } from '@/api/business/textbook'
import LazyPageImage from './components/LazyPageImage.vue'

const router = useRouter()
const loading = ref(false)
const books = ref([])
const keyword = ref('')
const downloadingBookId = ref(null)

const primaryGrades = [3, 4, 5, 6]
const juniorGrades = [7, 8, 9]

function matches(book) {
  const key = keyword.value.trim()
  if (!key) return true
  return `${book.title}${book.shortTitle}第${book.grade}册`.includes(key)
}

const visibleGroups = computed(() => {
  const build = (key, title, grades) => {
    const list = books.value
      .filter(book => grades.includes(Number(book.grade)) && matches(book))
      .sort((a, b) => (a.grade - b.grade) || String(a.semester).localeCompare(String(b.semester)))
    const pending = TEXTBOOK_PENDING_VOLUMES
      .filter(item => !list.some(book => Number(book.grade) === item.grade && String(book.semester) === item.semester))
      .filter(item => grades.includes(item.grade) && (!keyword.value.trim() || item.shortTitle.includes(keyword.value.trim())))
    return { key, title, books: list, pending }
  }
  return [
    build('primary', '小学', primaryGrades),
    build('junior', '初中', juniorGrades)
  ].filter(group => group.books.length || group.pending.length)
})

function openBook(book) {
  router.push(`/business/textbook/read/${book.bookId}`)
}

async function downloadBook(book) {
  if (downloadingBookId.value !== null) return
  downloadingBookId.value = book.bookId
  try {
    const blob = await downloadTextbookBookPdf(book.bookId)
    saveAs(blob, `${book.title || book.shortTitle || '电子教材'}.pdf`)
  } catch (error) {
    ElMessage.error(error?.message || '整册 PDF 下载失败，请稍后重试')
  } finally {
    downloadingBookId.value = null
  }
}

async function load() {
  loading.value = true
  try {
    const res = await listTextbookBooks()
    books.value = res.data || []
  } catch (error) {
    ElMessage.error('教材列表加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.textbook-shelf {
  padding-bottom: 24px;
}

.shelf-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 18px;
}

.shelf-title {
  margin: 0 0 6px;
  font-size: 20px;
  color: #303133;
}

.shelf-sub {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

.shelf-search {
  width: 220px;
}

.shelf-body {
  min-height: 220px;
}

.stage-block + .stage-block {
  margin-top: 26px;
}

.stage-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  padding-left: 10px;
  border-left: 3px solid #409eff;
}

.book-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(168px, 1fr));
  gap: 18px;
}

.book-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.book-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(64, 158, 255, 0.18);
  border-color: #c6e2ff;
}

.book-card.is-pending {
  cursor: default;
  opacity: 0.62;
  border-style: dashed;
}

.book-card.is-pending:hover {
  transform: none;
  box-shadow: none;
  border-color: #ebeef5;
}

.book-cover {
  aspect-ratio: 3 / 4;
  overflow: hidden;
  border-radius: 4px;
  background: #f5f7fa;
}

.book-cover :deep(.textbook-page) {
  height: 100%;
  box-shadow: none;
}

.pending-cover {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 34px;
  color: #c0c4cc;
}

.book-name {
  margin-top: 10px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.book-meta {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}

.book-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-top: 8px;
}
.book-open {
  font-size: 12px;
  color: #409eff;
}
.book-download {
  padding: 0;
  font-size: 12px;
}

.shelf-tip {
  margin: 22px 0 0;
  font-size: 12px;
  color: #a8abb2;
}
</style>
