import vision.gears.webglmath.Vec2
import kotlin.math.*

class UFO(target: Avatar, mass: Float, momentOfInertia: Float, dragCoeffs: Vec2, vararg meshes: Mesh):
    PhysicsObject(mass, momentOfInertia, dragCoeffs, *meshes){
    private val thrustGain = 1.8f
    override val tag = Tag.UFO
    init {
        calcForce = { _ ->
            val delta = target.position - position
            delta * thrustGain
        }

        calcTorque = { _ -> 0.0f}
    }
    override fun kinematics(dt: Float) {
        if (velocity.lengthSquared() > 0.001) {
            yaw = atan2(velocity.y, velocity.x)
        }
        super.kinematics(dt)
    }
}