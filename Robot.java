/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team5576.robot;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Encoder;

public class Robot extends IterativeRobot {
	
	 VictorSP d_right = new VictorSP(0);
	 VictorSP d_left = new VictorSP(1);
	 
	 DifferentialDrive m_robotDrive = new DifferentialDrive(d_left, d_right);
	
	 VictorSP lift = new VictorSP(2);
	
	 //Refrencing sensors for autonomous
	 Encoder encoder = new Encoder(1, 2); //find stuff for encoder
	 ADXRS450_Gyro gyro = new ADXRS450_Gyro();	 
	 
	 Joystick m_stick = new Joystick(0);
	 Timer m_timer = new Timer();
	 Compressor blow = new Compressor();
	 DoubleSolenoid clamp = new DoubleSolenoid(0, 7);
	 DoubleSolenoid pivit = new DoubleSolenoid(1, 6);
	 DoubleSolenoid shifter = new DoubleSolenoid(2, 5);
	 boolean toggle = true;
	 boolean toggle1 = true;
	 boolean clampState = false;
	 final String defaultAuto = "Default";
	 final String rightAuto = "Right Auto";
	 final String leftAuto = "Left Auto";
	 final String centerAuto = "Center Auto";
	 final String testStraightandTurns = "Correction Test Auto";
	 final String testDistance = "Accel and Deccel Auto";
	 final String testLiftDistance = "Lift Test Auto";
	 SendableChooser<String> chooser = new SendableChooser<>();	 
	 
	 //auto and sensor variables
	//Direction orientation using gyro (Variables)
	//change varables to make robot turn different directions
	double correction = 0; //this is used to actually turn your robot
	double tolerance = 1;//degree of tolerance
	double desiredAngle; //angle you are trying to reach ex. 0 is straight, 90 is a right turn, -90 is a left turn etc
	double turnSpeed = 0.6; //speed at which you are correcting your angle

	//math for distance calculations (Variables)
	double wheelDiameter = 6; //Diameter of wheels being used
	double pi = 3.1415;  //calls PI 3.1415....
	double circumference = (wheelDiameter * pi);  //calculates distance around wheel(total travel per one revolution)
	int ticksPerRotation = 360;  //360 becuase of the type of encoder
	double distancePerTick = (circumference/ticksPerRotation); //distance per one Tick
	
	double desiredDistance; //this is your desired distance your wanting to travel
	double desiredTickCount = (desiredDistance/distancePerTick); //calculated #of ticks based on distance and ticks per 1 rotation
	double actualTickCount = encoder.get();     //Recieves actual tick count

	//variables needed to change: desired distance

	//encoder variables
	double topSpeed = 0.8;
	double bottomSpeed = 0.35;
	int distanceToStartStop = 200; //measured in ticks (20 ticks per one inch  200 = 10in)
	double ratio = (bottomSpeed/topSpeed);
	double finalXSpeed = 0; 

	//math for deceleration (Variables)
	double deccelFinalPercent = (Math.pow(ratio, 1 / distanceToStartStop));

	//math for accel (Variables)
	double accelFinalPercent = (Math.pow(Math.pow(ratio, -1), 1 / distanceToStartStop));

	String gameData;
	int step;
	int totalCount;
	int iterationCount;
	 
	 
	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */

	 public void robotInit() {
		chooser.addDefault("Default Auto", defaultAuto);
		chooser.addObject("Right Auto", rightAuto);
		chooser.addObject("Left Auto", leftAuto);
		chooser.addObject("Center Auto", centerAuto);
		chooser.addObject("Correction Test Auto" , testStraightandTurns);
		chooser.addObject("Lift Test Auto", testLiftDistance);
		chooser.addObject("Accel and Deccel Auto", testDistance);
		SmartDashboard.putData("Auto modes", chooser);
		SmartDashboard.putNumber("Actual Tick Count", actualTickCount);
		SmartDashboard.putNumber("Actual Distance", (actualTickCount * distancePerTick));
		SmartDashboard.putNumber("Actual Angle", gyro.getAngle());
		SmartDashboard.putNumber("Current Turn Speed", correction);
		SmartDashboard.putNumber("Current X Speed", finalXSpeed);
		
		
	}

	/**
	 * This function is run once each time the robot enters autonomous mode.
	 */
	@Override
	public void autonomousInit() {
		m_timer.reset();
		m_timer.start();
		step = 0;
		encoder.reset();
		gyro.reset();
		
	}

	/**
	 * This function is called periodically during autonomous.
	 */
	@Override
	public void autonomousPeriodic() {
		
		gameData = DriverStation.getInstance().getGameSpecificMessage();
		  
		String autoSelected = chooser.getSelected();
		// String autoSelected = SmartDashboard.getString("Auto Selector",
		// defaultAuto);
		System.out.println("Auto selected: " + autoSelected);
		

		switch (autoSelected) {
		
		case testStraightandTurns:
			
		      desiredAngle = 0;

	        if (gyro.getAngle() < (desiredAngle - tolerance)) {
		          correction = turnSpeed;
		        }else if (gyro.getAngle() > (desiredAngle + tolerance)) {
		          correction = (-1 * turnSpeed);
		        }else{
		          correction = 0;
		        }
			
		case testDistance:
			
		      desiredDistance = 30;
		      desiredTickCount = desiredDistance/distancePerTick;
		      desiredAngle = 0;
			
		      if((desiredTickCount-actualTickCount) <= distanceToStartStop && (desiredTickCount-actualTickCount) > 0){
			          finalXSpeed = 0.8*Math.pow(accelFinalPercent, 200-(desiredTickCount-actualTickCount)); //using formular to accelerate from 0.35 to 0.8
			        } else if ((desiredTickCount-actualTickCount) <= 0){
			          finalXSpeed = 0;  //if desired distance is traveled set motors to 0
			          step = 1;
			        }
			        if(actualTickCount <= distanceToStartStop){
			          finalXSpeed = 0.8*Math.pow(deccelFinalPercent, actualTickCount); //using formula to decelerate from 0.8 to 0.35
			        }
			  
		
		case testLiftDistance:
		
		
		
		case leftAuto:
			  if(gameData.charAt(0) == 'L'){
				    
				    if(step == 0){ // you need to set variables for each step that will change... ex. desired distance, angle, and reset gyro reading
				      clamp.set(Value.kReverse);
				      
				      desiredDistance = 145;
				      desiredTickCount = desiredDistance/distancePerTick;
				      desiredAngle = 0;
				      
				        if (gyro.getAngle() < (desiredAngle - tolerance)) {
				          correction = turnSpeed;
				        }else if (gyro.getAngle() > (desiredAngle + tolerance)) {
				          correction = (-1 * turnSpeed);
				        }else{
				          correction = 0;
				        }
				          
				        if((desiredTickCount-actualTickCount) <= distanceToStartStop && (desiredTickCount-actualTickCount) > 0){
				          finalXSpeed = 0.8*Math.pow(accelFinalPercent, 200-(desiredTickCount-actualTickCount)); //using formular to accelerate from 0.35 to 0.8
				        } else if ((desiredTickCount-actualTickCount) <= 0){
				          finalXSpeed = 0;  //if desired distance is traveled set motors to 0
				          step = 1;
				        }
				        if(actualTickCount <= distanceToStartStop){
				          finalXSpeed = 0.8*Math.pow(deccelFinalPercent, actualTickCount); //using formula to decelerate from 0.8 to 0.35
				        }
				  
				    
				    }else if(step == 1){//turn 90
				      desiredAngle = 90;
				      totalCount = 0;
				      
				        if (gyro.getAngle() < (desiredAngle - tolerance)) {
				          correction = turnSpeed;
				        }else if (gyro.getAngle() > (desiredAngle + tolerance)) {
				          correction = (-1 * turnSpeed);
				        }else if (correction == 0 && totalCount == 15){
				          step = 2;
				          encoder.reset();
				          gyro.reset();
				        }else{
				          correction = 0;
				          totalCount = (totalCount + 1);
				        }
				        
				    }else if(step == 2){
				      desiredDistance = 19.3;
				      desiredTickCount = desiredDistance/distancePerTick;
				      desiredAngle = 0;
				      iterationCount = 0;
				      
				        if (gyro.getAngle() < (desiredAngle - tolerance)) {
				          correction = turnSpeed;
				        }else if (gyro.getAngle() > (desiredAngle + tolerance)) {
				          correction = (-1 * turnSpeed);
				        }else{
				          correction = 0;
				        }
				          
				        if((desiredTickCount-actualTickCount) <= distanceToStartStop && (desiredTickCount-actualTickCount) > 0){
				          finalXSpeed = 0.8*Math.pow(deccelFinalPercent, 200-(desiredTickCount-actualTickCount)); //using formular to accelerate from 0.35 to 0.8
				        } else if ((desiredTickCount-actualTickCount) <= 0){
				          finalXSpeed = 0;  //if desired distance is traveled set motors to 0
				          step = 3;
				        }
				        if(actualTickCount <= distanceToStartStop){
				          finalXSpeed = 0.8*Math.pow(accelFinalPercent, actualTickCount); //using formula to decelerate from 0.8 to 0.35
				        }
				        
				        if(iterationCount < 150){ //stops after 3 seconds
				          lift.set(0.4);
				          iterationCount = (iterationCount + 1);
				        }else{
				          lift.set(0);
				        }
				        
				    }else if(step == 3){
				      
				      if(iterationCount < 300){
				        pivit.set(Value.kForward);
				        iterationCount = (iterationCount + 1);
				      }else{
				        step = 4;
				      }
				    }else if(step == 4){
				      clamp.set(Value.kForward);
				    }
				  }else{
				    desiredDistance = 101;
				    desiredTickCount = desiredDistance/distancePerTick;
				    desiredAngle = 0;
				        
				        //Direction orientation using gyro (Code)
				    if (gyro.getAngle() < (desiredAngle - tolerance)) {
				      correction = turnSpeed;
				    }else if (gyro.getAngle() > (desiredAngle + tolerance)) {
				      correction = (-1 * turnSpeed);
				    }else{
				      correction = 0;
				    }

				//Drive set distance with Acceleration and Decceleration
				    if((desiredTickCount-actualTickCount) <= distanceToStartStop && (desiredTickCount-actualTickCount) > 0){
				      finalXSpeed = 0.8*Math.pow(deccelFinalPercent, 200-(desiredTickCount-actualTickCount)); //using formular to accelerate from 0.35 to 0.8
				    }else if ((desiredTickCount-actualTickCount) <= 0){
				      finalXSpeed = 0;  //if desired distance is traveled set motors to 0
				    }
				    if(actualTickCount <= distanceToStartStop){
				      finalXSpeed = 0.8*Math.pow(accelFinalPercent, actualTickCount); //using formula to decelerate from 0.8 to 0.35
				    }
				  }

		case rightAuto:
			  if(gameData.charAt(0) == 'R'){
				    
				    if(step == 0){ // you need to set variables for each step that will change... ex. desired distance, angle, and reset gyro reading
				      clamp.set(Value.kReverse);
				      
				      desiredDistance = 145;
				      desiredTickCount = desiredDistance/distancePerTick;
				      desiredAngle = 0;
				      
				        if (gyro.getAngle() < (desiredAngle - tolerance)) {
				          correction = turnSpeed;
				        }else if (gyro.getAngle() > (desiredAngle + tolerance)) {
				          correction = (-1 * turnSpeed);
				        }else{
				          correction = 0;
				        }
				          
				        if((desiredTickCount-actualTickCount) <= distanceToStartStop && (desiredTickCount-actualTickCount) > 0){
				          finalXSpeed = 0.8*Math.pow(deccelFinalPercent, 200-(desiredTickCount-actualTickCount)); //using formular to accelerate from 0.35 to 0.8
				        } else if ((desiredTickCount-actualTickCount) <= 0){
				          finalXSpeed = 0;  //if desired distance is traveled set motors to 0
				          step = 1;
				        }
				        if(actualTickCount <= distanceToStartStop){
				          finalXSpeed = 0.8*Math.pow(accelFinalPercent, actualTickCount); //using formula to decelerate from 0.8 to 0.35
				        }
				  
				    
				    }else if(step == 1){//turn 90
				      desiredAngle = -90;
				      totalCount = 0;
				      
				        if (gyro.getAngle() < (desiredAngle - tolerance)) {
				          correction = turnSpeed;
				        }else if (gyro.getAngle() > (desiredAngle + tolerance)) {
				          correction = (-1 * turnSpeed);
				        }else if (correction == 0 && totalCount == 15){
				          step = 2;
				          encoder.reset();
				          gyro.reset();
				        }else{
				          correction = 0;
				          totalCount = (totalCount + 1);
				        }
				        
				    }else if(step == 2){
				      desiredDistance = 19.3;
				      desiredTickCount = desiredDistance/distancePerTick;
				      desiredAngle = 0;
				      iterationCount = 0;
				      
				        if (gyro.getAngle() < (desiredAngle - tolerance)) {
				          correction = turnSpeed;
				        }else if (gyro.getAngle() > (desiredAngle + tolerance)) {
				          correction = (-1 * turnSpeed);
				        }else{
				          correction = 0;
				        }
				          
				        if((desiredTickCount-actualTickCount) <= distanceToStartStop && (desiredTickCount-actualTickCount) > 0){
				          finalXSpeed = 0.8*Math.pow(deccelFinalPercent, 200-(desiredTickCount-actualTickCount)); //using formular to accelerate from 0.35 to 0.8
				        } else if ((desiredTickCount-actualTickCount) <= 0){
				          finalXSpeed = 0;  //if desired distance is traveled set motors to 0
				          step = 3;
				        }
				        if(actualTickCount <= distanceToStartStop){
				          finalXSpeed = 0.8*Math.pow(accelFinalPercent, actualTickCount); //using formula to decelerate from 0.8 to 0.35
				        }
				        
				        if(iterationCount < 150){ //stops after 3 seconds  3000ms/20ms
				          lift.set(0.4);
				          iterationCount = (iterationCount + 1);
				        }else{
				          lift.set(0);
				        }
				        
				    }else if(step == 3){
				      
				      if(iterationCount < 300){
				        pivit.set(Value.kForward);
				        iterationCount = (iterationCount + 1);
				      }else{
				        step = 4;
				      }
				    }else if(step == 4){
				      clamp.set(Value.kForward);
				    }
				  }else{
				    desiredDistance = 101;
				    desiredTickCount = desiredDistance/distancePerTick;
				    desiredAngle = 0;
				        
				        
				    if (gyro.getAngle() < (desiredAngle - tolerance)) {
				      correction = turnSpeed;
				    }else if (gyro.getAngle() > (desiredAngle + tolerance)) {
				      correction = (-1 * turnSpeed);
				    }else{
				      correction = 0;
				    }
				      
				    if((desiredTickCount-actualTickCount) <= distanceToStartStop && (desiredTickCount-actualTickCount) > 0){
				      finalXSpeed = 0.8*Math.pow(deccelFinalPercent, 200-(desiredTickCount-actualTickCount)); //using formular to accelerate from 0.35 to 0.8
				    }else if ((desiredTickCount-actualTickCount) <= 0){
				      finalXSpeed = 0;  //if desired distance is traveled set motors to 0
				    }
				    if(actualTickCount <= distanceToStartStop){
				      finalXSpeed = 0.8*Math.pow(accelFinalPercent, actualTickCount); //using formula to decelerate from 0.8 to 0.35
				    }
				  }

		case centerAuto:
			  if(gameData.charAt(0) == 'R'){
				  
				  if(step == 0){ // you need to set variables for each step that will change... ex. desired distance, angle, and reset gyro reading
				      clamp.set(Value.kReverse);
				      
				      desiredDistance = 47.5;
				      desiredTickCount = desiredDistance/distancePerTick;
				      desiredAngle = 0;
				      
				        if (gyro.getAngle() < (desiredAngle - tolerance)) {
				          correction = turnSpeed;
				        }else if (gyro.getAngle() > (desiredAngle + tolerance)) {
				          correction = (-1 * turnSpeed);
				        }else{
				          correction = 0;
				        }
				          
				        if((desiredTickCount-actualTickCount) <= distanceToStartStop && (desiredTickCount-actualTickCount) > 0){
				          finalXSpeed = 0.8*Math.pow(deccelFinalPercent, 200-(desiredTickCount-actualTickCount)); //using formular to accelerate from 0.35 to 0.8
				        } else if ((desiredTickCount-actualTickCount) <= 0){
				          finalXSpeed = 0;  //if desired distance is traveled set motors to 0
				          step = 1;
				        }
				        if(actualTickCount <= distanceToStartStop){
				          finalXSpeed = 0.8*Math.pow(accelFinalPercent, actualTickCount); //using formula to decelerate from 0.8 to 0.35
				        }
				          
				    }else if(step == 1){//turn 90
				      desiredAngle = 90;
				      totalCount = 0;
				      
				        if (gyro.getAngle() < (desiredAngle - tolerance)) {
				          correction = turnSpeed;
				        }else if (gyro.getAngle() > (desiredAngle + tolerance)) {
				          correction = (-1 * turnSpeed);
				        }else if (correction == 0 && totalCount == 15){
				          step = 2;
				          encoder.reset();
				          gyro.reset();
				        }else{
				          correction = 0;
				          totalCount = (totalCount + 1);
				        }
				        
				    }else if(step == 2){
				      desiredDistance = 53.25;
				      desiredTickCount = desiredDistance/distancePerTick;
				      desiredAngle = 0;
				      iterationCount = 0;
				      
				    if (gyro.getAngle() < (desiredAngle - tolerance)) {
				      correction = turnSpeed;
				    }else if (gyro.getAngle() > (desiredAngle + tolerance)) {
				      correction = (-1 * turnSpeed);
				    }else{
				      correction = 0;
				    }
				        
				    if((desiredTickCount-actualTickCount) <= distanceToStartStop && (desiredTickCount-actualTickCount) > 0){
				      finalXSpeed = 0.8*Math.pow(deccelFinalPercent, 200-(desiredTickCount-actualTickCount)); //using formular to accelerate from 0.35 to 0.8
				    } else if ((desiredTickCount-actualTickCount) <= 0){
				      finalXSpeed = 0; //if desired distance is traveled set motors to 0
				      step = 3;
				    }
				    if(actualTickCount <= distanceToStartStop){
				      finalXSpeed = 0.8*Math.pow(accelFinalPercent, actualTickCount); //using formula to decelerate from 0.8 to 0.35
				    }
				        
				        
				    }else if(step == 3){//turn 90
				      desiredAngle = -90;
				      totalCount = 0;
				      
				        if (gyro.getAngle() < (desiredAngle - tolerance)) {
				          correction = turnSpeed;
				        }else if (gyro.getAngle() > (desiredAngle + tolerance)) {
				          correction = (-1 * turnSpeed);
				        }else if (correction == 0 && totalCount == 15){
				          step = 4;
				          encoder.reset();
				          gyro.reset();
				        }else{
				          correction = 0;
				          totalCount = (totalCount + 1);
				        }
				        
				    }else if(step == 4){
				      desiredDistance = 54.5;
				      desiredTickCount = desiredDistance/distancePerTick;
				      desiredAngle = 0;
				      
				    if (gyro.getAngle() < (desiredAngle - tolerance)) {
				      correction = turnSpeed;
				    }else if (gyro.getAngle() > (desiredAngle + tolerance)) {
				      correction = (-1 * turnSpeed);
				    }else{
				      correction = 0;
				    }
				        
				    if((desiredTickCount-actualTickCount) <= distanceToStartStop && (desiredTickCount-actualTickCount) > 0){
				      finalXSpeed = 0.8*Math.pow(deccelFinalPercent, 200-(desiredTickCount-actualTickCount)); //using formular to accelerate from 0.35 to 0.8
				    } else if ((desiredTickCount-actualTickCount) <= 0){
				      finalXSpeed = 0;  //if desired distance is traveled set motors to 0
				      step = 5;
				    }
				    if(actualTickCount <= distanceToStartStop){
				      finalXSpeed = 0.8*Math.pow(accelFinalPercent, actualTickCount); //using formula to decelerate from 0.8 to 0.35
				    }
				    if(iterationCount < 150){ //stops after 3 seconds
				      lift.set(0.4);
				      iterationCount = (iterationCount + 1);
				    }else{
				      lift.set(0);
				        }
				          
				    }else if(step == 5){
				      
				      if(iterationCount < 300){
				        pivit.set(Value.kForward);
				        iterationCount = (iterationCount + 1);
				      }else{
				        step = 6;
				      }
				      
				    }else if(step == 6){
				      clamp.set(Value.kForward);
				    }	
			  
				   
					}else{ //code for center auto left side
					    
					    if(step == 0){ // you need to set variables for each step that will change... ex. desired distance, angle, and reset gyro reading
					      clamp.set(Value.kReverse); 
					      desiredDistance = 47.5;
					      desiredTickCount = desiredDistance/distancePerTick;
					      desiredAngle = 0;
					      
					        if (gyro.getAngle() < (desiredAngle - tolerance)) {
					          correction = turnSpeed;
					        }else if (gyro.getAngle() > (desiredAngle + tolerance)) {
					          correction = (-1 * turnSpeed);
					        }else{
					          correction = 0;
					        }
					          
					        if((desiredTickCount-actualTickCount) <= distanceToStartStop && (desiredTickCount-actualTickCount) > 0){
						      finalXSpeed = 0.8*Math.pow(deccelFinalPercent, 200-(desiredTickCount-actualTickCount)); //using formular to accelerate from 0.35 to 0.8
					        } else if ((desiredTickCount-actualTickCount) <= 0){
					          finalXSpeed = 0;  //if desired distance is traveled set motors to 0
					          step = 1;
					        }
					        if(actualTickCount <= distanceToStartStop){
					          finalXSpeed = 0.8*Math.pow(accelFinalPercent, actualTickCount); //using formula to decelerate from 0.8 to 0.35
					        }
					          
					    }else if(step == 1){//turn 90
					      desiredAngle = -90;
					      totalCount = 0;
					      
					        if (gyro.getAngle() < (desiredAngle - tolerance)) {
					          correction = turnSpeed;
					        }else if (gyro.getAngle() > (desiredAngle + tolerance)) {
					          correction = (-1 * turnSpeed);
					        }else if (correction == 0 && totalCount == 15){
					          step = 2;
					          encoder.reset();
					          gyro.reset();
					        }else{
					          correction = 0;
					          totalCount = (totalCount + 1);
					        }
					        
					    }else if(step == 2){
					      desiredDistance = 53.7;
					      desiredTickCount = desiredDistance/distancePerTick;
					      desiredAngle = 0;
					      iterationCount = 0;
					      
					    if (gyro.getAngle() < (desiredAngle - tolerance)) {
					      correction = turnSpeed;
					    }else if (gyro.getAngle() > (desiredAngle + tolerance)) {
					      correction = (-1 * turnSpeed);
					    }else{
					      correction = 0;
					    }
					        
					    if((desiredTickCount-actualTickCount) <= distanceToStartStop && (desiredTickCount-actualTickCount) > 0){
					      finalXSpeed = 0.8*Math.pow(deccelFinalPercent, 200-(desiredTickCount-actualTickCount)); //using formular to accelerate from 0.35 to 0.8
					    } else if ((desiredTickCount-actualTickCount) <= 0){
					      finalXSpeed = 0; //if desired distance is traveled set motors to 0
					      step = 3;
					    }
					    if(actualTickCount <= distanceToStartStop){
					      finalXSpeed = 0.8*Math.pow(accelFinalPercent, actualTickCount); //using formula to decelerate from 0.8 to 0.35
					    }
					        
					        
					    }else if(step == 3){//turn 90
					      desiredAngle = 90;
					      totalCount = 0;
					      
					        if (gyro.getAngle() < (desiredAngle - tolerance)) {
					          correction = turnSpeed;
					        }else if (gyro.getAngle() > (desiredAngle + tolerance)) {
					          correction = (-1 * turnSpeed);
					        }else if (correction == 0 && totalCount == 15){
					          step = 4;
					          encoder.reset();
					          gyro.reset();
					        }else{
					          correction = 0;
					          totalCount = (totalCount + 1);
					        }
					        
					    }else if(step == 4){
					      desiredDistance = 54.45;
					      desiredTickCount = desiredDistance/distancePerTick;
					      desiredAngle = 0;
					      
					    if (gyro.getAngle() < (desiredAngle - tolerance)) {
					      correction = turnSpeed;
					    }else if (gyro.getAngle() > (desiredAngle + tolerance)) {
					      correction = (-1 * turnSpeed);
					    }else{
					      correction = 0;
					    }
					        
					    if((desiredTickCount-actualTickCount) <= distanceToStartStop && (desiredTickCount-actualTickCount) > 0){
					      finalXSpeed = 0.8*Math.pow(deccelFinalPercent, 200-(desiredTickCount-actualTickCount)); //using formular to accelerate from 0.35 to 0.8
					    } else if ((desiredTickCount-actualTickCount) <= 0){
					      finalXSpeed = 0;  //if desired distance is traveled set motors to 0
					      step = 5;
					    }
					    if(actualTickCount <= distanceToStartStop){
					      finalXSpeed = 0.8*Math.pow(accelFinalPercent, actualTickCount); //using formula to decelerate from 0.8 to 0.35
					    }
					    if(iterationCount < 150){ //stops after 3 seconds
					      lift.set(0.4);
					      iterationCount = (iterationCount + 1);
					    }else{
					      lift.set(0);
					    }
					          
					    }else if(step == 300){
					      
					      if(iterationCount < 3000){
					        pivit.set(Value.kForward);
					        iterationCount = (iterationCount + 1);
					      }else{
					        step = 6;
					      }
					    }else if(step == 6){
					      clamp.set(Value.kForward);
					    }
					    
			m_robotDrive.arcadeDrive(finalXSpeed, correction);
			
					}
		}
	}

	/**
	 * This function is called once each time the robot enters teleoperated mode.
	 */
	@Override
	public void teleopInit() {
	}

	
	@Override
	public void teleopPeriodic() {
		m_robotDrive.arcadeDrive(m_stick.getY(), m_stick.getZ(), true);
		
		blow.setClosedLoopControl(true);
	
	if (toggle && m_stick.getRawButton(1)) {
		toggle = false;
		if(clampState) {
			clamp.set(Value.kReverse);
			clampState = false;
		}else {
			clamp.set(Value.kForward);
			clampState = true;
		}
	} else if (!m_stick.getRawButton(1)) {
		toggle = true;
	}
	    
	if (toggle && m_stick.getRawButton(2)) {
		toggle1 = false;
		if(clampState) {
			pivit.set(Value.kReverse);
			clampState = false;
		}else {
			pivit.set(Value.kForward);
			clampState = true;
		}
	} else if (!m_stick.getRawButton(2)) {
		toggle1 = true;
	}
	        
	
	
	
	    if(m_stick.getPOV() == -1) {
	    	lift.set(0);
	    }else if(m_stick.getPOV()== 180) {
	    	lift.set(1);
	    }else if(m_stick.getPOV() == 0) {
	    	lift.set(-0.5);
	    }
	    
	    if(m_stick.getRawAxis(3) > 0) {
	    	shifter.set(Value.kForward);
	    }else if(m_stick.getRawAxis(3) < 0) {
	    	shifter.set(Value.kReverse);
	    }
	
		
	
	    	    
	   

}
	/**
	 * This function is called periodically during test mode.
	 */
	@Override
	public void testPeriodic() {
	}
}
