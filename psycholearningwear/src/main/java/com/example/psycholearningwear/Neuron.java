package com.example.psycholearningwear;
import java.lang.Math;

public class Neuron {

    private double input = 0; //value the neuron holds
    private boolean isInputLayer = false;
    private boolean isOutputLayer = false;
    private boolean isBias = false;
    private double output; //calculated output
    private double target; //target-value after feeding the net
    private double learning_rate = 0.1; //rate, how fast the net learns -- faster = less accurate
    private double[] delta_w; //delta_weights
    private double[] weights;


    Neuron(int x, boolean is_input){

        //create a neuron with number of weights = number of neurons of the following layer

        if(is_input) { //create one more weight for bias of hidden-layer

            weights = new double[x+1];

        }

        else{weights = new double[x];}

    }

    void init_delta_w(int x){

        //initialize delta-weights with updatable 0-values

        delta_w = new double[x];

        for(int i=0; i<x; i++){

            delta_w[i] = 0.0;

        }
    }

    double f(double x){

        //sigmoid-function to calculate output in an interval of [0,1]

        return Math.pow(1 + Math.exp(-x),-1);
    }

    double f_d(double x){

        //derivative of sigmoid function, is needed to calculate gradients

        return Math.exp(x)/Math.pow((Math.exp(x)+1),2);
    }

    void incr_input(double x){

        input += x;
    }

    void reset_input(){

        input = 0;
    }

    void set_InputLayer(){

        isOutputLayer = !isInputLayer;
    }

    void set_OutputLayer(){

        isOutputLayer = !isOutputLayer;
    }

    double calc_output(){

        //calculate output f(x) for every neuron in hidden- and input-layer, that is not a bias

        if(isOutputLayer && !isBias){ //output-layer holds input values as output values

            output = input;
            return output;
        }

        else if(!isBias){

            output = f(input);
            return output;
        }

        else{ //bias-value = 1

            output = 1;
            return  output;
        }
    }

    double get_output(){

        return output;
    }

    void set_bias(){

        isBias = !isBias;
    }

    double get_input(){

        return input;
    }

    double get_target(){

        return target;
    }

    double[] get_delta_w(){

        //get delta-weights

        return delta_w;
    }

    void set_delta_w(int x, double w){

        delta_w[x] = w;
    }

    double[] return_weights(){

        return  weights;
    }

    void set_target(double w){

        target = w;
    }

    void set_weight(int i, double w){

        weights[i] += w;
    }

    void set_specific_weight(int i, double w){

        weights[i] = w;
    }

    double[] output_gradient(Neuron[] layer){

        //gradient for the output layer

        double[] gradients = new double[layer.length];
        for(int i=0; i<layer.length; i++){

            //update gradients by using delta-rule

            gradients[i] = (2*learning_rate*(target-f(input))*f_d(input)*layer[i].get_output());
        }

        return gradients;
    }

    double[] hidden_gradient(Neuron[] layer, Neuron[] output_layer){

        //gradients for the hidden-layer

        double[] gradients = new double[layer.length];

        double sum = 0;
        for(int i=0; i<output_layer.length; i++){

            sum += (output_layer[i].get_target() - output_layer[i].get_output())*
                    output_layer[i].f_d(output_layer[i].get_input())*weights[i];
        }

        for(int i=0; i<layer.length; i++){

            gradients[i] = 2*learning_rate*sum*f_d(input)*layer[i].get_output();
        }

        return  gradients;
    }

}


