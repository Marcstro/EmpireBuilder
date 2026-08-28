package buildings;

public interface AttackCapableBuilding {

     AttackCapableBuildingComponent getAttackCapableBuildingComponent();

     default double getX(){
            return getAttackCapableBuildingComponent().getBuilding().getX();
     }
     default double getY(){
            return getAttackCapableBuildingComponent().getBuilding().getY();
     }
}
