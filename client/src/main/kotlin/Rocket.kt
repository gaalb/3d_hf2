import vision.gears.webglmath.*
import kotlin.math.*

private const val EPS = 1e-8f
fun Vec2.signedAngleTo(other: Vec2): Float {
    val aLen = this.length()
    val bLen = other.length()
    if (aLen < EPS || bLen < EPS) return 0f
    val dot = (this dot other)
    val crossZ = this.x * other.y - this.y * other.x
    return atan2(crossZ.toDouble(), dot.toDouble()).toFloat()
}

class Rocket(target: Avatar, mass: Float, momentOfInertia: Float, dragCoeffs: Vec2, vararg meshes: Mesh):
    PhysicsObject(mass, momentOfInertia, dragCoeffs, *meshes){
    private val thrust = 2f
    private val Kp     = 10.0f
    private val EPS    = 1e-6f
    override val tag = Tag.CHASER
    init {
        calcForce = { _ ->
            val forward = Vec2(cos(yaw), sin(yaw))
            forward * thrust
        }

        calcTorque = { _ ->
            val toTarget = target.position - this.position
            if (toTarget.length() < EPS) 0f else {
                val forward = Vec2(cos(yaw), sin(yaw))
                val err = forward.signedAngleTo(toTarget)   // (-pi, pi)
                Kp * err
            }
        }
    }
}