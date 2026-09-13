<script setup lang="ts">defineProps<{ label: string; value: string | number; accent?: string; note: string }>()</script>
<template><article class="metric-card" :style="{ '--accent': accent ?? '#37d7ff' }"><span>{{ label }}</span><strong>{{ value }}</strong><small>{{ note }}</small></article></template>
