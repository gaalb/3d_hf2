import vision.gears.webglmath.*
import kotlin.math.*

class Drag(var velocityCoeff: Float, var spinCoeff: Float, val eps: Float = 1e-6f) {
    fun force(p: PhysicsObject): Vec2 {
        val fwd = Vec2(cos(p.yaw), sin(p.yaw)) // unit pointing forward
        val vPar  = fwd * (p.velocity dot fwd) // parallel velocity component
        val vPerp = p.velocity - vPar // perpendicular velocity component
        val kPar  = p.dragCoeffs.x * velocityCoeff
        val kPerp = p.dragCoeffs.y * velocityCoeff
        val fPar  = if (vPar.lengthSquared()  > eps) vPar  * vPar.length()  * kPar  else Vec2()
        val fPerp = if (vPerp.lengthSquared() > eps) vPerp * vPerp.length() * kPerp else Vec2()
        return (fPar + fPerp) * -1f
    }

    fun torque(physicsObject: PhysicsObject): Float {
        return -physicsObject.angVel.pow(2) * spinCoeff * sign(physicsObject.angVel)
    }


}