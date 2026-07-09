//
// Guitar Case Monument - OpenSCAD
$fn=80;
baseLength=180; baseWidth=100; baseHeight=40;
caseHeight=220; caseThickness=18;
plaqueWidth=110; plaqueHeight=28; plaqueDepth=2;
module marbleBase(){
 difference(){
  cube([baseLength,baseWidth,baseHeight]);
  translate([(baseLength-plaqueWidth)/2,-0.01,baseHeight/2-plaqueHeight/2])
   rotate([90,0,0]) linear_extrude(plaqueDepth+0.02)
    square([plaqueWidth,plaqueHeight]);
 }
}
module guitarCase2D(){
 hull(){
  translate([0,150]) circle(r=18);
  translate([0,90]) circle(r=42);
  translate([0,35]) circle(r=55);
  translate([0,-55]) circle(r=42);
  translate([0,-105]) circle(r=24);
 }
}
module guitarCase(){linear_extrude(caseThickness) guitarCase2D();}
union(){
 marbleBase();
 translate([baseLength/2,baseWidth/2-caseThickness/2,baseHeight])
  rotate([90,0,0]) guitarCase();
}
