import { test } from 'node:test';
import assert from 'node:assert/strict';
import { redoContextKey, isTheoryRedoPending } from '../theoryRedo.js';

test('重做范围必须是单班单课，切班和切课不能复用选择', () => {
  assert.equal(redoContextKey([1, 2], '2025', '2'), '');
  assert.equal(redoContextKey([1], '2025', null), '');
  assert.notEqual(redoContextKey([1], '2025', '2'), redoContextKey([1], '2025', '3'));
  assert.notEqual(redoContextKey([1], '2025', '2'), redoContextKey([2], '2025', '2'));
});
test('待重做与首次未答、重做得零分必须区分', () => {
  assert.equal(isTheoryRedoPending({ revision: 0, answerCount: 0 }), false);
  assert.equal(isTheoryRedoPending({ revision: 4, answerCount: 0 }), true);
  assert.equal(isTheoryRedoPending({ revision: 4, answerCount: 3 }), false);
  assert.equal(isTheoryRedoPending(undefined), false);
});
