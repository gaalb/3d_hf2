import vision.gears.webglmath.UniformProvider
import vision.gears.webglmath.*


open class GameObject(vararg meshes: Mesh) : UniformProvider("gameObject") {
    val position = Vec2()
    var yaw = 0.0f
    val scale = Vec3(1.0f, 1.0f, 1.0f)
    val modelMatrix by Mat4()
    init {
        addComponentsAndGatherUniforms(*meshes)
    }

    open class Motion(val gameObject: GameObject) {
        open operator fun invoke(
            dt: Float,
            t: Float,
            keysPressed: Set<String>,
            interactors: ArrayList<GameObject>,
            spawn: ArrayList<GameObject>
        ): Boolean {
            return true
        }
    }

    fun collide(gameObject: GameObject) {

    }
    fun update() {
        modelMatrix.set()
            .scale(scale)
            .rotate(yaw, 0f, 0f, 1f)
            .translate(position)
    }


}