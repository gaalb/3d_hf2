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


    fun update() {
        modelMatrix.set()
            .scale(scale)
            .rotate(yaw, 0f, 0f, 1f)
            .translate(position)
    }


}