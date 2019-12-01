package com.ryandw11.structure.utils;

public enum RotEnum {
	
	D90(0.5 * Math.PI, 90),
	D180(1*Math.PI, 180),
	D270(1.5*Math.PI, 270),
	D0(0, 0);
	
	public final double rad;
	public final int deg;
	
	private RotEnum(double rad, int deg) {
		this.rad = rad;
		this.deg = deg;
	}
}
