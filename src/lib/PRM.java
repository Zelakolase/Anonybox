package lib;

import java.math.BigInteger;
import java.util.ArrayList;

public class PRM {

    ArrayList<Integer> lcf = new ArrayList<Integer>();
    ArrayList<Integer> primRoots = new ArrayList<Integer>();

    public ArrayList<Integer> getPrimitiveRoots(int n){

        if(isPrime(n)){
           n = n-1;
        }else{
            return null;
        }

        ArrayList<Integer> lowestCommonFactors = lowestCommonFactors(n);
        ArrayList<Integer> dividers = new ArrayList<Integer>();

        for (int i = 0; i < lowestCommonFactors.size(); i++) {
            dividers.add(n/lowestCommonFactors.get(i));
        }

        loop: for (int i = 2; i <= n; i++) {
            for (int j = 0; j < dividers.size(); j++) {
//                System.out.println(i + " " + divider.get(j));

                BigInteger modded = performModPow(i, dividers.get(j),n+1);

                if(modded.compareTo(new BigInteger("1")) == 0){
                    continue loop;
                }
                if(j == dividers.size()-1){
                    primRoots.add(i);
                }
            }

        }

        return primRoots;
    }


    public BigInteger performModPow(int powee, int power, int mod){

        BigInteger bigPowee = new BigInteger(powee +"");
        BigInteger bigPower = new BigInteger(power+"");
        BigInteger bigMod = new BigInteger(mod+"");

        return bigPowee.modPow(bigPower,bigMod);

    }





    public ArrayList<Integer> lowestCommonFactors(int number){
        int count;
        for (int i = 2; i<=(number); i++) {
            count = 0;
            while (number % i == 0) {
                number /= i;
                count++;
            }
            if (count == 0) {
                continue;
            }
            lcf.add(i);
        }
        return lcf;
    }




    public boolean isPrime(int x){
        if(x<2){return false;}
        for (int i = 2; i <= x/2; i++) {
            if(x%i == 0){ return false; }
        }
        return true;
    }

}