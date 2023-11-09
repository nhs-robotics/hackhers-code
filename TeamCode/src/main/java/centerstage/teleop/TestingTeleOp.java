package centerstage.teleop;

import static centerstage.Constants.ROBOT;

import com.pocolifo.robobase.BuildProperties;
import com.pocolifo.robobase.bootstrap.TeleOpOpMode;
import com.pocolifo.robobase.control.GamepadCarWheels;
import com.pocolifo.robobase.control.Pressable;
import com.pocolifo.robobase.control.Toggleable;
import com.pocolifo.robobase.motor.CarWheels;
import com.pocolifo.robobase.motor.DoubleCRServo;
import com.pocolifo.robobase.motor.DoubleMotor;
import com.pocolifo.robobase.motor.Motor;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.*;

@TeleOp(name = "TeleOp " + BuildProperties.VERSION)
public class TestingTeleOp extends TeleOpOpMode {
    private CarWheels carWheels;
    private GamepadCarWheels gamepadCarWheels;
    private Toggleable useMicroMovement;
    private Pressable intake;
    private Motor intakeMotor;
    boolean intakeOpen = false;
    private Pressable LaunchPlane;
    private Pressable outtake;
    private Servo outtakeServo;
    boolean outtakeRunning = false;
    private DoubleCRServo midjoint;
    private Pressable midjointToTop;
    private Pressable midjointToBottom;
    private Pressable midjointForward;
    private Pressable midjointBackward;
    private DoubleMotor linearSlides;
    private Pressable fullReset;
    private Pressable dpadUp;
    private Pressable dpadDown;

    @Override
    public void initialize() {
        this.carWheels = new CarWheels(
                hardwareMap,
                1120,
                10d,
                ROBOT,
                "FL",
                "FR",
                "BL",
                "BR",
                "FL"
        );
//Gamepad 1
        this.gamepadCarWheels = new GamepadCarWheels(this.carWheels, this.gamepad1);

        this.useMicroMovement = new Toggleable(() -> this.gamepad1.a);

        this.intake = new Pressable(() -> this.gamepad1.right_bumper);
        this.intakeMotor = new Motor(hardwareMap.get(DcMotor.class,"intake"), 1120);

        this.LaunchPlane = new Pressable(() -> this.gamepad1.b);

//Gamepad 2
        //Todo: may need to tweak coefficients
        this.linearSlides = new DoubleMotor(new Motor(hardwareMap.get(DcMotor.class, "motor1"), 1120), new Motor(hardwareMap.get(DcMotor.class, "motor2"), 1120), 1, 1);
        this.dpadUp = new Pressable(() -> this.gamepad2.dpad_up);
        this.dpadDown = new Pressable(() -> this.gamepad2.dpad_down);

        this.midjoint = new DoubleCRServo(hardwareMap.get(CRServo.class,"servo1"), hardwareMap.get(CRServo.class,"servo2"), 0.5);
        this.midjointToTop = new Pressable(() -> this.gamepad2.x);
        this.midjointToBottom = new Pressable(() -> this.gamepad2.b);
        this.midjointForward = new Pressable(() -> (this.gamepad2.right_trigger > 0.5));
        this.midjointBackward = new Pressable(() -> (this.gamepad2.left_trigger > 0.5));


        this.outtake = new Pressable(() -> this.gamepad2.right_bumper);
        this.outtakeServo = hardwareMap.get(Servo.class, "outtakeServo");


    }

    //Todo: May also need tweaking
    private void rotateMidjointForward()
    {
        midjoint.spin(DcMotorSimple.Direction.FORWARD);
    }
    private void rotateMidjointBackward()
    {
        midjoint.spin(DcMotorSimple.Direction.REVERSE);
    }

    @Override
    //NOTE: It might be nice to have a processUpdates function that takes an array of togglables and updates all of them
    public void loop() {
        //Gamepad 1
            //Driving
                useMicroMovement.processUpdates();
                this.gamepadCarWheels.updateWithDpadDrive(this.useMicroMovement.get());

            //Intake
                if (this.intake.get() && !intakeOpen) {
                    intakeMotor.drive(1);
                    intakeOpen = true;
                }
                else if (!intake.get() && intakeOpen)
                {
                    intakeMotor.drive(0);
                    intakeOpen = false;
                }

            //Launch Plane
                if (this.LaunchPlane.get()) {
                    //launch the plane!
                }


        //Gamepad 2
            //Linear Slides
            linearSlides.spin(gamepad2.left_stick_y/4); //4 is arbitrary, but full power seems like a lot
                //Or it might use the D-pad:
            if(dpadUp.get())
            {
                linearSlides.spin(0.25);
            }
            else if (dpadDown.get())
            {
                linearSlides.spin(0.25);
            }
            else
            {
                linearSlides.stopMoving();
            }

        //Midjoint
                if(fullReset.get())
                {
                    //Todo: while not at center limit switch
                    //Todo: rotate in proper direction
                    linearSlides.spin(-1);

                    //May want to split this into two threads. Or make linear slide code not waste time

                    while (linearSlides.getPosition() > 5) //5 is arbitrary, may want a bigger/smaller number
                    {
                        //do nothing
                    }
                    linearSlides.stopMoving();
                }
                else if (midjointToTop.get())
                {
                    //Todo: while not at top limit switch
                    rotateMidjointForward();
                    midjoint.stopMoving();
                }
                else if (midjointToBottom.get()) {
                    //Todo: while not at bottom limit switch
                    rotateMidjointBackward();
                    midjoint.stopMoving();
                }
                else if (midjointForward.get())
                {
                        rotateMidjointForward();
                }
                else if (midjointBackward.get())
                {
                        rotateMidjointBackward();
                }
                else
                {
                    midjoint.stopMoving();
                }
        //Secondary thread: midjoint tracker. Not sure how to implement yet, but used for the full reset

        //Outtake
            if (this.outtake.get() && !outtakeRunning) {
                outtakeServo.setPosition(1);
                outtakeRunning = true;
            }
            else if (!intake.get() && outtakeRunning)
            {
                outtakeServo.setPosition(0);
                outtakeRunning = false;
            }
    }
}
