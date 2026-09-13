<script setup lang="ts">defineProps<{ title: string; phase: string }>()</script>
<template><section class="page"><header class="page-heading"><div><p class="eyebrow">MODULE RESERVED</p><h1>{{ title }}</h1></div></header><div class="state-panel">该模块将在 {{ phase }} 按阶段验收标准接入，当前没有静态假功能。</div></section></template>
