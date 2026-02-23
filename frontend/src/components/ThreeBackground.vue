<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as THREE from 'three'

const props = defineProps({
  particleCount: { type: Number, default: 120 },
  color1: { type: String, default: '#667eea' },
  color2: { type: String, default: '#764ba2' },
  speed: { type: Number, default: 0.3 }
})

const canvasRef = ref(null)
let scene, camera, renderer, particles, geometryShapes
let mouseX = 0, mouseY = 0
let animationId = null

const init = () => {
  const container = canvasRef.value
  if (!container) return

  scene = new THREE.Scene()
  camera = new THREE.PerspectiveCamera(75, container.clientWidth / container.clientHeight, 0.1, 1000)
  camera.position.z = 30

  renderer = new THREE.WebGLRenderer({ alpha: true, antialias: true })
  renderer.setSize(container.clientWidth, container.clientHeight)
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.setClearColor(0x000000, 0)
  container.appendChild(renderer.domElement)

  // Floating particles
  const particleGeometry = new THREE.BufferGeometry()
  const positions = new Float32Array(props.particleCount * 3)
  const velocities = new Float32Array(props.particleCount * 3)

  for (let i = 0; i < props.particleCount; i++) {
    positions[i * 3] = (Math.random() - 0.5) * 60
    positions[i * 3 + 1] = (Math.random() - 0.5) * 40
    positions[i * 3 + 2] = (Math.random() - 0.5) * 30
    velocities[i * 3] = (Math.random() - 0.5) * 0.02
    velocities[i * 3 + 1] = (Math.random() - 0.5) * 0.02
    velocities[i * 3 + 2] = (Math.random() - 0.5) * 0.02
  }

  particleGeometry.setAttribute('position', new THREE.BufferAttribute(positions, 3))
  const particleMaterial = new THREE.PointsMaterial({
    size: 0.15,
    color: new THREE.Color(props.color1),
    transparent: true,
    opacity: 0.6,
    blending: THREE.AdditiveBlending
  })
  particles = new THREE.Points(particleGeometry, particleMaterial)
  particles.userData.velocities = velocities
  scene.add(particles)

  // Floating geometric shapes
  geometryShapes = new THREE.Group()

  const shapes = [
    new THREE.IcosahedronGeometry(1.2, 0),
    new THREE.OctahedronGeometry(1, 0),
    new THREE.TetrahedronGeometry(0.9, 0),
    new THREE.TorusGeometry(0.8, 0.3, 8, 16),
    new THREE.DodecahedronGeometry(0.7, 0),
  ]

  for (let i = 0; i < 8; i++) {
    const geo = shapes[i % shapes.length]
    const mat = new THREE.MeshBasicMaterial({
      color: new THREE.Color(i % 2 === 0 ? props.color1 : props.color2),
      wireframe: true,
      transparent: true,
      opacity: 0.15
    })
    const mesh = new THREE.Mesh(geo, mat)
    mesh.position.set(
      (Math.random() - 0.5) * 50,
      (Math.random() - 0.5) * 30,
      (Math.random() - 0.5) * 20 - 5
    )
    mesh.userData.rotSpeed = {
      x: (Math.random() - 0.5) * 0.01,
      y: (Math.random() - 0.5) * 0.01
    }
    mesh.userData.floatSpeed = Math.random() * 0.005 + 0.002
    mesh.userData.floatOffset = Math.random() * Math.PI * 2
    geometryShapes.add(mesh)
  }
  scene.add(geometryShapes)

  // Connection lines between close particles
  const lineMaterial = new THREE.LineBasicMaterial({
    color: new THREE.Color(props.color1),
    transparent: true,
    opacity: 0.08
  })
  const lineGeometry = new THREE.BufferGeometry()
  const linePositions = new Float32Array(props.particleCount * props.particleCount * 6)
  lineGeometry.setAttribute('position', new THREE.BufferAttribute(linePositions, 3))
  const lines = new THREE.LineSegments(lineGeometry, lineMaterial)
  lines.userData.isLines = true
  scene.add(lines)
}

const animate = () => {
  animationId = requestAnimationFrame(animate)

  const time = Date.now() * 0.001

  // Animate particles
  if (particles) {
    const positions = particles.geometry.attributes.position.array
    const velocities = particles.userData.velocities

    for (let i = 0; i < props.particleCount; i++) {
      positions[i * 3] += velocities[i * 3] * props.speed
      positions[i * 3 + 1] += velocities[i * 3 + 1] * props.speed
      positions[i * 3 + 2] += velocities[i * 3 + 2] * props.speed

      // Wrap around
      if (Math.abs(positions[i * 3]) > 30) velocities[i * 3] *= -1
      if (Math.abs(positions[i * 3 + 1]) > 20) velocities[i * 3 + 1] *= -1
      if (Math.abs(positions[i * 3 + 2]) > 15) velocities[i * 3 + 2] *= -1
    }
    particles.geometry.attributes.position.needsUpdate = true

    // Mouse interaction
    particles.rotation.x += (mouseY * 0.0001 - particles.rotation.x) * 0.05
    particles.rotation.y += (mouseX * 0.0001 - particles.rotation.y) * 0.05
  }

  // Animate shapes
  if (geometryShapes) {
    geometryShapes.children.forEach((mesh) => {
      mesh.rotation.x += mesh.userData.rotSpeed.x
      mesh.rotation.y += mesh.userData.rotSpeed.y
      mesh.position.y += Math.sin(time * mesh.userData.floatSpeed * 100 + mesh.userData.floatOffset) * 0.01
    })
  }

  // Update connection lines
  const lines = scene.children.find(c => c.userData?.isLines)
  if (lines && particles) {
    const pPos = particles.geometry.attributes.position.array
    const lPos = lines.geometry.attributes.position.array
    let lineIdx = 0
    const maxDist = 8

    for (let i = 0; i < Math.min(props.particleCount, 50); i++) {
      for (let j = i + 1; j < Math.min(props.particleCount, 50); j++) {
        const dx = pPos[i * 3] - pPos[j * 3]
        const dy = pPos[i * 3 + 1] - pPos[j * 3 + 1]
        const dz = pPos[i * 3 + 2] - pPos[j * 3 + 2]
        const dist = Math.sqrt(dx * dx + dy * dy + dz * dz)

        if (dist < maxDist && lineIdx < lPos.length - 6) {
          lPos[lineIdx++] = pPos[i * 3]
          lPos[lineIdx++] = pPos[i * 3 + 1]
          lPos[lineIdx++] = pPos[i * 3 + 2]
          lPos[lineIdx++] = pPos[j * 3]
          lPos[lineIdx++] = pPos[j * 3 + 1]
          lPos[lineIdx++] = pPos[j * 3 + 2]
        }
      }
    }

    // Clear remaining
    for (let i = lineIdx; i < lPos.length; i++) lPos[i] = 0
    lines.geometry.attributes.position.needsUpdate = true
    lines.geometry.setDrawRange(0, lineIdx / 3)
  }

  renderer.render(scene, camera)
}

const onMouseMove = (e) => {
  mouseX = e.clientX - window.innerWidth / 2
  mouseY = e.clientY - window.innerHeight / 2
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
