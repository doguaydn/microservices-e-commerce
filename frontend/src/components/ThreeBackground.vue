<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as THREE from 'three'

const props = defineProps({
  mode: { type: String, default: 'galaxy' }, // 'galaxy' | 'subtle'
  particleCount: { type: Number, default: 3000 },
  speed: { type: Number, default: 0.15 }
})

const canvasRef = ref(null)
let scene, camera, renderer, galaxyPoints, nebulaPoints, centralMesh, auroras
let mouseX = 0, mouseY = 0, targetMouseX = 0, targetMouseY = 0
let animationId = null
let clock

const init = () => {
  const container = canvasRef.value
  if (!container) return

  clock = new THREE.Clock()
  scene = new THREE.Scene()

  camera = new THREE.PerspectiveCamera(60, container.clientWidth / container.clientHeight, 0.1, 2000)
  camera.position.z = props.mode === 'subtle' ? 50 : 35

  renderer = new THREE.WebGLRenderer({ alpha: true, antialias: true })
  renderer.setSize(container.clientWidth, container.clientHeight)
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.setClearColor(0x000000, 0)
  container.appendChild(renderer.domElement)

  if (props.mode === 'galaxy') {
    createGalaxy()
    createNebula()
    createCentralGeometry()
    createAuroraRibbons()
  } else {
    createSubtleParticles()
  }
}

// ===== GALAXY SPIRAL =====
const createGalaxy = () => {
  const count = props.particleCount
  const geometry = new THREE.BufferGeometry()
  const positions = new Float32Array(count * 3)
  const colors = new Float32Array(count * 3)
  const sizes = new Float32Array(count)

  const colorInside = new THREE.Color('#ff6030')
  const colorMid = new THREE.Color('#667eea')
  const colorOutside = new THREE.Color('#00f0ff')

  const branches = 5
  const spin = 2.5
  const radius = 20

  for (let i = 0; i < count; i++) {
    const i3 = i * 3
    const r = Math.random() * radius
    const branchAngle = ((i % branches) / branches) * Math.PI * 2
    const spinAngle = r * spin

    const randomX = Math.pow(Math.random(), 3) * (Math.random() < 0.5 ? 1 : -1) * r * 0.15
    const randomY = Math.pow(Math.random(), 3) * (Math.random() < 0.5 ? 1 : -1) * r * 0.08
    const randomZ = Math.pow(Math.random(), 3) * (Math.random() < 0.5 ? 1 : -1) * r * 0.15

    positions[i3] = Math.cos(branchAngle + spinAngle) * r + randomX
    positions[i3 + 1] = randomY
    positions[i3 + 2] = Math.sin(branchAngle + spinAngle) * r + randomZ

    const mixedColor = colorInside.clone()
    const t = r / radius
    if (t < 0.5) {
      mixedColor.lerp(colorMid, t * 2)
    } else {
      mixedColor.copy(colorMid).lerp(colorOutside, (t - 0.5) * 2)
    }

    colors[i3] = mixedColor.r
    colors[i3 + 1] = mixedColor.g
    colors[i3 + 2] = mixedColor.b

    sizes[i] = Math.random() * 2 + 0.5
  }

  geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3))
  geometry.setAttribute('color', new THREE.BufferAttribute(colors, 3))
  geometry.setAttribute('size', new THREE.BufferAttribute(sizes, 1))

  const material = new THREE.PointsMaterial({
    size: 0.12,
    vertexColors: true,
    transparent: true,
    opacity: 0.85,
    blending: THREE.AdditiveBlending,
    depthWrite: false,
    sizeAttenuation: true,
  })

  galaxyPoints = new THREE.Points(geometry, material)
  galaxyPoints.rotation.x = -0.4
  scene.add(galaxyPoints)
}

// ===== NEBULA CLOUD =====
const createNebula = () => {
  const count = 800
  const geometry = new THREE.BufferGeometry()
  const positions = new Float32Array(count * 3)
  const colors = new Float32Array(count * 3)

  const nebulaColors = [
    new THREE.Color('#764ba2'),
    new THREE.Color('#667eea'),
    new THREE.Color('#00c9ff'),
    new THREE.Color('#ff6b6b'),
  ]

  for (let i = 0; i < count; i++) {
    const i3 = i * 3
    const theta = Math.random() * Math.PI * 2
    const phi = Math.acos(2 * Math.random() - 1)
    const r = 8 + Math.random() * 18

    positions[i3] = r * Math.sin(phi) * Math.cos(theta)
    positions[i3 + 1] = (Math.random() - 0.5) * 12
    positions[i3 + 2] = r * Math.sin(phi) * Math.sin(theta)

    const c = nebulaColors[Math.floor(Math.random() * nebulaColors.length)]
    colors[i3] = c.r
    colors[i3 + 1] = c.g
    colors[i3 + 2] = c.b
  }

  geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3))
  geometry.setAttribute('color', new THREE.BufferAttribute(colors, 3))

  const material = new THREE.PointsMaterial({
    size: 0.35,
    vertexColors: true,
    transparent: true,
    opacity: 0.3,
    blending: THREE.AdditiveBlending,
    depthWrite: false,
  })

  nebulaPoints = new THREE.Points(geometry, material)
  scene.add(nebulaPoints)
}

// ===== CENTRAL GLOWING GEOMETRY =====
const createCentralGeometry = () => {
  const geometry = new THREE.TorusKnotGeometry(2.5, 0.6, 128, 32, 2, 3)
  const material = new THREE.MeshBasicMaterial({
    color: '#667eea',
    wireframe: true,
    transparent: true,
    opacity: 0.12,
  })
  centralMesh = new THREE.Mesh(geometry, material)
  scene.add(centralMesh)

  // Inner glow sphere
  const glowGeo = new THREE.SphereGeometry(1.8, 32, 32)
  const glowMat = new THREE.MeshBasicMaterial({
    color: '#764ba2',
    transparent: true,
    opacity: 0.06,
  })
  const glow = new THREE.Mesh(glowGeo, glowMat)
  scene.add(glow)
}

// ===== AURORA RIBBONS =====
const createAuroraRibbons = () => {
  auroras = new THREE.Group()

  for (let r = 0; r < 3; r++) {
    const points = []
    const segments = 80
    for (let i = 0; i <= segments; i++) {
      const t = (i / segments) * Math.PI * 2
      const x = Math.cos(t) * (12 + r * 3)
      const y = Math.sin(t * 3 + r * 1.5) * 2.5
      const z = Math.sin(t) * (12 + r * 3)
      points.push(new THREE.Vector3(x, y, z))
    }

    const curve = new THREE.CatmullRomCurve3(points, true)
    const tubeGeometry = new THREE.TubeGeometry(curve, 100, 0.08, 8, true)
    const colors = ['#00f0ff', '#667eea', '#ff6b6b']
    const material = new THREE.MeshBasicMaterial({
      color: colors[r],
      transparent: true,
      opacity: 0.08,
      blending: THREE.AdditiveBlending,
      side: THREE.DoubleSide,
    })
    const tube = new THREE.Mesh(tubeGeometry, material)
    tube.userData.baseY = 0
    tube.userData.speed = 0.3 + r * 0.15
    tube.userData.offset = r * Math.PI * 0.66
    auroras.add(tube)
  }

  scene.add(auroras)
}

// ===== SUBTLE MODE (for global background) =====
const createSubtleParticles = () => {
  const count = 400
  const geometry = new THREE.BufferGeometry()
  const positions = new Float32Array(count * 3)
  const colors = new Float32Array(count * 3)

  const c1 = new THREE.Color('#667eea')
  const c2 = new THREE.Color('#764ba2')
  const c3 = new THREE.Color('#00c9ff')
  const palette = [c1, c2, c3]

  for (let i = 0; i < count; i++) {
    const i3 = i * 3
    positions[i3] = (Math.random() - 0.5) * 100
    positions[i3 + 1] = (Math.random() - 0.5) * 60
    positions[i3 + 2] = (Math.random() - 0.5) * 40 - 10

    const c = palette[Math.floor(Math.random() * palette.length)]
    colors[i3] = c.r
    colors[i3 + 1] = c.g
    colors[i3 + 2] = c.b
  }

  geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3))
  geometry.setAttribute('color', new THREE.BufferAttribute(colors, 3))

  const material = new THREE.PointsMaterial({
    size: 0.08,
    vertexColors: true,
    transparent: true,
    opacity: 0.4,
    blending: THREE.AdditiveBlending,
    depthWrite: false,
  })

  galaxyPoints = new THREE.Points(geometry, material)
  scene.add(galaxyPoints)
}

// ===== ANIMATION LOOP =====
const animate = () => {
  animationId = requestAnimationFrame(animate)
  const elapsed = clock.getElapsedTime()

  // Smooth mouse
  mouseX += (targetMouseX - mouseX) * 0.05
  mouseY += (targetMouseY - mouseY) * 0.05

  if (galaxyPoints) {
    galaxyPoints.rotation.y = elapsed * props.speed * 0.1
    if (props.mode === 'galaxy') {
      galaxyPoints.rotation.y += mouseX * 0.00005
      galaxyPoints.rotation.x = -0.4 + mouseY * 0.00003
    } else {
      galaxyPoints.rotation.y += mouseX * 0.00002
      galaxyPoints.rotation.x += mouseY * 0.00001
    }
  }

  if (nebulaPoints) {
    nebulaPoints.rotation.y = elapsed * 0.02
    nebulaPoints.rotation.x = Math.sin(elapsed * 0.05) * 0.1
  }

  if (centralMesh) {
    centralMesh.rotation.x = elapsed * 0.15
    centralMesh.rotation.y = elapsed * 0.2
    const pulse = 1 + Math.sin(elapsed * 0.8) * 0.05
    centralMesh.scale.set(pulse, pulse, pulse)
  }

  if (auroras) {
    auroras.children.forEach((tube) => {
      tube.rotation.y = elapsed * tube.userData.speed * 0.1
      tube.position.y = Math.sin(elapsed * tube.userData.speed + tube.userData.offset) * 1.5
    })
  }

  renderer.render(scene, camera)
}

const onMouseMove = (e) => {
  targetMouseX = e.clientX - window.innerWidth / 2
  targetMouseY = e.clientY - window.innerHeight / 2
}

const onResize = () => {
  const container = canvasRef.value
  if (!container || !renderer) return
  camera.aspect = container.clientWidth / container.clientHeight
  camera.updateProjectionMatrix()
  renderer.setSize(container.clientWidth, container.clientHeight)
}

onMounted(() => {
  init()
  animate()
  window.addEventListener('mousemove', onMouseMove)
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  if (animationId) cancelAnimationFrame(animationId)
  window.removeEventListener('mousemove', onMouseMove)
  window.removeEventListener('resize', onResize)
  if (renderer) {
    renderer.dispose()
    const container = canvasRef.value
    if (container && renderer.domElement.parentNode === container) {
      container.removeChild(renderer.domElement)
    }
  }
})
</script>

<template>
  <div ref="canvasRef" class="three-bg"></div>
</template>

<style scoped>
.three-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
  z-index: 0;
  pointer-events: none;
}
</style>
