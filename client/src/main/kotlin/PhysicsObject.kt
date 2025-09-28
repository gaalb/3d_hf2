import vision.gears.webglmath.*
import kotlin.math.*

fun Vec2.rotateAssign(angle: Float): Vec2 {
    val c = cos(angle); val s = sin(angle)
    val px = x; val py = y
    x = c * px - s * py
    y = s * px + c * py
    return this
}

enum class Tag { AVATAR, CHASER, UFO, BULLET, NEUTRAL }

val DESTROY_EACH_OTHER = setOf(Tag.CHASER, Tag.UFO, Tag.BULLET)

open class PhysicsObject(val mass: Float, val momentOfInertia: Float, val dragCoeffs: Vec2,vararg meshes: Mesh) : GameObject(*meshes) {
    val velocity = Vec2()
    var angVel = 0.0f
    open val tag: Tag = Tag.NEUTRAL
    var isAlive: Boolean = true
    var calcForce: (keysPressed: Set<String>) -> Vec2 = {Vec2()}
    var calcTorque: (keysPressed: Set<String>) -> Float = {0.0f}
    var handleCollision: (other: GameObject) -> Unit = {}
    open fun kinematics(dt: Float) {
        position += velocity * dt
        yaw += angVel * dt
    }
    fun dynamics(dt: Float, force: Vec2, torque: Float) {
        val acc = force / mass
        val angAcc = torque / momentOfInertia
        velocity += acc * dt
        angVel += angAcc * dt
    }

    open fun update(dt: Float,
               keysPressed: Set<String>,
               interactors: ArrayList<GameObject>,
               drag: Drag) {
        for (other in interactors) {
            handleCollision(other)
        }
        val force = calcForce(keysPressed)
        force += drag.force(this)
        val torque = calcTorque(keysPressed) + drag.torque(this)
        dynamics(dt, force, torque)
        kinematics(dt)
        super.update()
    }
}