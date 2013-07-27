package com.sismics.docs.core.util.math;


/**
 * Classe utilitaire pour les calculs
 * 
 * @author bgamard
 * 
 */
public class MathUtil {

    /**
     * Arrondi à 2 décimales près
     * 
     * @param d Nombre à arrondir
     * @return Nombre arrondi
     */
    public static Double round(Double d) {
        return Math.round(d * 100.0) / 100.0;
    }
    
    /**
     * Contraint une valeur entre min et max.
     * 
     * @param value Valeur
     * @param min Minimum
     * @param max Maximum
     * @return Valeur contrainte
     */
    public static double clip(double value, double min, double max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    /**
     * Interpole une valeur entre deux points.
     * 
     * @param x Valeur à interpoler
     * @param x1 Point 1 (x)
     * @param y1 Point 1 (y)
     * @param x2 Point 2 (x)
     * @param y2 Point 2 (y)
     * @return Valeur interpolée
     */
    public static double interpolate(double x, double x1, double y1, double x2, double y2) {
        double alpha = (x - x1) / (x2 - x1);
        
        return y1 * (1 - alpha) + y2 * alpha;
    }
    
    /**
     * Retourne un Double depuis un Number.
     * 
     * @param number Number
     * @return Double
     */
    public static Double getDoubleFromNumber(Number number) {
        if (number == null) {
            return null;
        }
        
        return number.doubleValue();
    }
    
    /**
     * Retourne un Integer depuis un Number.
     * 
     * @param number Number
     * @return Integer
     */
    public static Integer getIntegerFromNumber(Number number) {
        if (number == null) {
            return null;
        }
        
        return number.intValue();
    }
    
    /**
     * Retourne un Long depuis un Number.
     * 
     * @param number Number
     * @return Long
     */
    public static Long getLongFromNumber(Number number) {
        if (number == null) {
            return null;
        }
        
        return number.longValue();
    }
}
