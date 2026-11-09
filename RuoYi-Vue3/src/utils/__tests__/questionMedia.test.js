import test from 'node:test'
import assert from 'node:assert/strict'
import { emptyQuestionMedia, normalizeQuestionMedia, hasChoiceOption, hasQuestionImages, slotImages } from '../questionMedia.js'
test('历史空媒体保持纯文字选项',()=>{assert.equal(hasQuestionImages({}),false);assert.equal(hasChoiceOption({optionA:'文字'},'A'),true);assert.equal(hasChoiceOption({},'B'),false)})
test('纯图片选项存在，正文不需要占位文字',()=>{const media=emptyQuestionMedia();media.options.B=[{imageId:'8'}];const q={media};assert.equal(hasChoiceOption(q,'B'),true);assert.equal(hasQuestionImages(q),true);assert.equal(slotImages(q,'B')[0].imageId,'8')})
test('独立归一化避免编辑污染列表或其他题目',()=>{const source=emptyQuestionMedia();source.stem.push({imageId:9});const copy=normalizeQuestionMedia(source);copy.stem[0].imageId='10';assert.equal(source.stem[0].imageId,9);assert.throws(()=>normalizeQuestionMedia({schemaVersion:2}))})
