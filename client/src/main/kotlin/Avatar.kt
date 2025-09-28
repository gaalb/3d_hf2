import vision.gears.webglmath.Vec2

class Avatar(mass: Float, momentOfInertia: Float, dragCoeffs: Vec2, vararg meshes: Mesh):
    PhysicsObject(mass, momentOfInertia, dragCoeffs, *meshes) {
    override val tag = Tag.AVATAR
    val cooldown = 1.0f
    var lastShot = 0.0f
    fun onSpace(t : Float) {
        if (t - lastShot > cooldown) {
            shoot()
            lastShot = t
        }
    }
    var shoot: () -> Unit = {}
    init {
        calcForce = {
                keysPressed: Set<String> ->
            val thrust = 1.2f
            if (keysPressed.contains("W")) Vec2(thrust, 0.0f).rotateAssign(yaw) else Vec2()
        }
        calcTorque = {
                keysPressed: Set<String> ->
            val M = 5.0f
            var torque = 0.0f
            if (keysPressed.contains("A")) {
                torque += M
            }
            if (keysPressed.contains("D")) {
                torque -= M
            }
            torque
        }
    }

}