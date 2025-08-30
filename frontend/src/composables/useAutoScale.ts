import { ref, onMounted, onBeforeUnmount, computed } from 'vue'

/**
 * 页面 / 卡片自动缩放（按窗口宽高同比例等比缩放）
 * @param designWidth  设计稿基准宽度（>= 此宽度时不再放大）
 * @param designHeight 设计稿基准高度（>= 此高度时不再放大）
 * @param minScale 最小缩放
 * @param maxScale 最大缩放
 */
export function useAutoScale(designWidth = 1280, designHeight = 800, minScale = 0.75, maxScale = 1) {
  const scale = ref(1)
  const calc = () => {
    const sw = window.innerWidth / designWidth
    const sh = window.innerHeight / designHeight
    let s = Math.min(sw, sh)
    if (s > maxScale) s = maxScale
    if (s < minScale) s = minScale
    scale.value = Number(s.toFixed(4))
  }
  onMounted(() => {
    calc()
    window.addEventListener('resize', calc)
  })
  onBeforeUnmount(() => window.removeEventListener('resize', calc))

  const style = computed(() => ({
    transform: `scale(${scale.value})`,
    transformOrigin: 'center center',
    // 让缩放后仍居中 (父容器 flex 居中) 不需要再位移
    willChange: 'transform'
  }))

  return { scale, style }
}
