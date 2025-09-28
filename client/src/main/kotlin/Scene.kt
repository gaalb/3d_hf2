import org.w3c.dom.HTMLCanvasElement
import org.khronos.webgl.WebGLRenderingContext as GL
import vision.gears.webglmath.*
import kotlin.js.Date
import kotlin.math.*
import kotlin.random.Random

class Scene(
    val gl: WebGL2RenderingContext
) : UniformProvider("scene") {
    val vsGameObject = Shader(gl, GL.VERTEX_SHADER, "camera-model-vs.glsl")
    val fsGameObject = Shader(gl, GL.FRAGMENT_SHADER, "material-fs.glsl")
    val gameObjectProgram = Program(gl, vsGameObject, fsGameObject)
    val quadGeometry = TexturedQuadGeometry(gl)
    val backgroundTexture = Texture2D(gl, "media/background_light.jpg")
    val backgroundMaterial = Material(gameObjectProgram).apply {
        this["colorTexture"]?.set(backgroundTexture)
        this["tint"]?.set(1f, 1f, 1f, 1f)
    }
    val background = GameObject(Mesh(backgroundMaterial, quadGeometry)).apply {
        scale.set(Vec3(16f, 9f))
    }
    val drag = Drag(0.1f, 0.5f)
    val raiderMaterial = Material(gameObjectProgram).apply {
        this["colorTexture"]?.set(Texture2D(gl, "media/raider.png"))
        this["tint"]?.set(1f, 1f, 1f, 1f)
    }
    val bulletTexture = Texture2D(gl, "media/explosion.png")
    val bulletMaterial = Material(gameObjectProgram).apply {
        this["colorTexture"]?.set(bulletTexture)
        this["tint"]?.set(1f, 1f, 1f)
    }

    val chaserMaterial = Material(gameObjectProgram).apply {
        this["colorTexture"]?.set(Texture2D(gl, "media/missile_cropped.png"))
        this["tint"]?.set(1f, 1f, 1f, 1f)
    }

    val ufoMaterial = Material(gameObjectProgram).apply {
        this["colorTexture"]?.set(Texture2D(gl, "media/ufo_cropped.png"))
        this["tint"]?.set(1f, 1f, 1f, 1f)
    }

    fun spawnBullet(avatar: Avatar) {
        val relativeVelocity = 2.0f
        val bullet = Bullet(
            0.1f,
            5.0f,
            Vec2(1.0f, 1.0f),
            Mesh(bulletMaterial, quadGeometry)
        ).apply {
            scale.set(0.1f, 0.1f, 0.1f)
            angVel = 20.0f
            val bulletDir = -1.0f * Vec2(cos(avatar.yaw), sin(avatar.yaw))
            position += avatar.position
            velocity += bulletDir * relativeVelocity + avatar.velocity
        }
        objects.add(bullet)
    }
    val avatar = Avatar(
        0.1f,
        0.25f,
        Vec2(1.0f, 100.0f),
        Mesh(raiderMaterial, quadGeometry)).apply {
        scale.set(0.1f, 0.1f, 0.1f)
        yaw = PI.toFloat()/2.0f
        shoot = { ->
            spawnBullet(this)
        }
    }
    val objects = ArrayList<PhysicsObject>()
    init {
        objects.add(avatar)
    }
    val camera = OrthoCamera()

    val timeAtFirstFrame = Date().getTime()
    var timeAtLastFrame = timeAtFirstFrame

    fun resize(gl: WebGL2RenderingContext, canvas: HTMLCanvasElement) {
        gl.viewport(0, 0, canvas.width, canvas.height)
        camera.setAspectRatio((canvas.width / canvas.height).toFloat())
    }

    var isPaused = false

    private val spawnInterval = 2.0f
    private var spawnAccum = 0.0f

    private val maxChasers = 12
    private val maxUfos    = 12

    private fun rand(min: Float, max: Float) =
        Random.nextFloat() * (max - min) + min

    private fun randomPointInBackground(): Vec2 {
        val hw = background.scale.x * 0.5f
        val hh = background.scale.y * 0.5f
        return Vec2(
            rand(background.position.x - hw, background.position.x + hw),
            rand(background.position.y - hh, background.position.y + hh)
        )
    }

    private fun spawnChaser() {
        val count = objects.count { it is Chaser }
        if (count >= maxChasers) return

        val p = randomPointInBackground()
        val ch = Chaser(
            avatar,
            0.1f, 0.5f, Vec2(1f, 100f),
            Mesh(chaserMaterial, quadGeometry)
        ).apply {
            scale.set(0.1f, 0.07f, 0.1f)
            position.set(p)
            yaw = atan2(avatar.position.y - p.y, avatar.position.x - p.x)
        }
        objects.add(ch)
    }

    private fun spawnUfo() {
        val count = objects.count { it is UFO }
        if (count >= maxUfos) return

        val p = randomPointInBackground()
        val u = UFO(
            avatar,
            0.1f,
            0.5f,
            Vec2(1.0f, 1.0f),
            Mesh(ufoMaterial, quadGeometry)
        ).apply {
            scale.set(0.08f, 0.08f, 0.08f)
            position.set(p)
        }
        objects.add(u)
    }

    private fun spawnEnemy() {
        val coin = Random.nextBoolean()
        val canChaser = objects.count { it is Chaser } < maxChasers
        val canUfo    = objects.count { it is UFO }    < maxUfos
        when {
            coin && canChaser -> spawnChaser()
            !coin && canUfo   -> spawnUfo()
            canChaser         -> spawnChaser()
            canUfo            -> spawnUfo()
            else              -> { /* both capped; do nothing */ }
        }
    }

    private fun shouldDestroy(a: PhysicsObject, b: PhysicsObject): Boolean {
        return (a.tag in DESTROY_EACH_OTHER &&
                b.tag in DESTROY_EACH_OTHER &&
                a.tag != b.tag)
    }

    fun collisionBox(o: PhysicsObject): Vec2 {
        return Vec2(o.scale.x*0.7f, o.scale.y*0.7f)
    }

    private fun overlap(a: PhysicsObject, b: PhysicsObject, square: Boolean = false): Boolean {
        val ha = collisionBox(a)
        val hb = collisionBox(b)

        val ax = if (square) max(ha.x, ha.y) else ha.x
        val ay = if (square) max(ha.x, ha.y) else ha.y
        val bx = if (square) max(hb.x, hb.y) else hb.x
        val by = if (square) max(hb.x, hb.y) else hb.y

        val dx = abs(a.position.x - b.position.x)
        val dy = abs(a.position.y - b.position.y)
        return dx <= (ax + bx) && dy <= (ay + by)
    }

    private fun handleCollisions() {
        var avatarHit = false
        for (i in 0 until objects.size) {
            val a = objects[i]; if (!a.isAlive) continue
            for (j in i + 1 until objects.size) {
                val b = objects[j]; if (!b.isAlive) continue

                val overlap = overlap(a, b, square = false)
                if (overlap) {
                    // Paint avatar red if collision avatar<->(UFO|Chaser)
                    val aIsAvatar = a is Avatar
                    val bIsAvatar = b is Avatar
                    val otherIsThreat = (a is UFO || a is Chaser || b is UFO || b is Chaser)

                    if ((aIsAvatar || bIsAvatar) && otherIsThreat) {
                        avatarHit = true
                    }
                    if (shouldDestroy(a, b)) {
                        a.isAlive = false
                        b.isAlive = false
                    }
                }
            }
        }
        if (avatarHit) {
            raiderMaterial["tint"]?.set(1f, 0f, 0f, 1f)
            backgroundMaterial["tint"]?.set(1f, 0f, 0f, 1f)
        } else {
            raiderMaterial["tint"]?.set(1f, 1f, 1f, 1f)
            backgroundMaterial["tint"]?.set(1f, 1f, 1f, 1f)
        }
    }

    private fun sweepDead() {
        objects.removeAll { !it.isAlive }
    }

    fun onKeyDown(keyCode: String) {
        if (keyCode == "Escape" || keyCode == "ESCAPE") isPaused = !isPaused
        var t = Date().getTime().toFloat()/1000.0f
        if (!isPaused && (keyCode == "Space" || keyCode == "SPACE")) avatar.onSpace(t)
    }

    fun update(gl: WebGL2RenderingContext, keysPressed: Set<String>) {
        val timeAtThisFrame = Date().getTime()
        val dt = (timeAtThisFrame - timeAtLastFrame).toFloat() / 1000.0f
        timeAtLastFrame = timeAtThisFrame

        gl.enable(GL.BLEND)
        gl.blendFunc(GL.SRC_ALPHA, GL.ONE_MINUS_SRC_ALPHA)
        gl.clearColor(0.2f, 0.0f, 0.5f, 1.0f)
        gl.clearDepth(1.0f)
        gl.clear(GL.COLOR_BUFFER_BIT or GL.DEPTH_BUFFER_BIT)

        gl.useProgram(gameObjectProgram.glProgram)

        camera.position.set(avatar.position.x, avatar.position.y)
        camera.update()

        background.update()
        background.draw(camera)

        if (!isPaused) {

            spawnAccum += dt
            while (spawnAccum >= spawnInterval) {
                spawnEnemy()
                spawnAccum -= spawnInterval
            }

            objects.forEach {
                it.update(dt, keysPressed, ArrayList<GameObject>(), drag)
            }

            handleCollisions()
            sweepDead()
        }

        val hw = background.scale.x
        val hh = background.scale.y
        val dx = avatar.position.x - background.position.x
        val dy = avatar.position.y - background.position.y
        if (kotlin.math.abs(dx) > hw || kotlin.math.abs(dy) > hh) {
            objects.remove(avatar)
            backgroundMaterial["tint"]?.set(1f, 0f, 0f, 1f)
            isPaused = true
        }

        objects.forEach { it.draw(camera) }
    }
}
