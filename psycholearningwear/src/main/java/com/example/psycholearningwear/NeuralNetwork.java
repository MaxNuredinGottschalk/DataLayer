package com.example.psycholearningwear;


public class NeuralNetwork {

    //neural network to make predictions

    //3 layers for the network
    private Neuron[] input_layer;
    private Neuron[] hidden_layer;
    private Neuron[] output_layer;

    private double alpha = 0.1; //learning rate


    NeuralNetwork(int a, int b, int c){

        //constructor for a net with a, b and c as number of neurons for each layer

        input_layer = new Neuron[a+1]; //input-layer with a+1 neurons including a bias

        for(int i=1; i<=a+1; i++){

            input_layer[i-1] = new Neuron(b,true); //fill input-layer with neurons, number of weights = #hidden_layer Neurons
            input_layer[i-1].set_InputLayer(); //is input-layer
        }
        input_layer[a].set_bias(); //bias with value 1.0

        hidden_layer = new Neuron[b+1]; //hidden-layer with a+1 neurons including a bias

        for(int i=1; i<=b+1; i++){

            hidden_layer[i-1] = new Neuron(c,false); //fill hidden-layer  with neurons, number of weights = #output_layer Neurons
            hidden_layer[i-1].init_delta_w(a+1); //initialize delta-weights with number of input-layer-neurons
        }
        hidden_layer[b].set_bias(); //bias with value 1.0

        output_layer = new Neuron[c]; //output-layer with c neurons, without bias

        for(int i=1; i<=c; i++){

            output_layer[i-1] = new Neuron(0,false); //fill output layer with neurons without weights
            output_layer[i-1].init_delta_w(b+1); //initialize delta weights with number of hidden-layer-neurons
            output_layer[i-1].set_OutputLayer(); //is output layer
        }

    }

    double[] get_layer_outputs(Neuron[] layer){

        //get outputs of a specific layer

        double[] outputs = new double[layer.length];
        for(int i=0; i<layer.length; i++){

            outputs[i] = layer[i].get_output();

        }

        return outputs;
    }


    double[] get_outputs(){

        //get outputs of net(output-layer)

        double[] outputs = new double[output_layer.length];
        for(int i=0; i<output_layer.length; i++){

            outputs[i] = output_layer[i].get_output();
        }

        return outputs;
    }


    Neuron[] get_hidden_layer(){

        return hidden_layer;
    }

    Neuron[] get_input_layer(){

        return input_layer;
    }

    Neuron[] get_output_layer(){

        return output_layer;
    }

    void feed_forward(double[] input){

        //take input to calculate output

        //increase values by multiplying weights with outputs

        for(int i=0; i<input_layer.length; i++){ //start with input-layer

            input_layer[i].incr_input(input[i]);

            for(int j=0; j<hidden_layer.length; j++){

                hidden_layer[j].incr_input(input_layer[i].calc_output()*
                        input_layer[i].return_weights()[j]);
            }
        }

        for(int i=0; i<hidden_layer.length; i++){ //continue with hidden-layer

            for(int j=0; j<output_layer.length; j++){

                output_layer[j].incr_input(hidden_layer[i].calc_output()*hidden_layer[i].return_weights()[j]);
            }
        }

        for(int i=0; i<output_layer.length; i++){ //calculate output values

            output_layer[i].calc_output();
        }

    }

    void reset_inputs(){ //set values to 0 for every layer

        for(int i=0; i<input_layer.length; i++){

            input_layer[i].reset_input();
        }

        for(int i=0; i<hidden_layer.length; i++){

            hidden_layer[i].reset_input();
        }

        for(int i=0; i<output_layer.length; i++){

            output_layer[i].reset_input();
        }
    }

    void back_prop(double[] output){

        //adjust weight-values

        for(int i=0; i<output.length; i++){ //start with output-layer

            for(int j=0; j<hidden_layer.length; j++){ //number of weights for every hidden-layer-neuron = #output-layer neurons

                hidden_layer[j].set_weight(i,(1-alpha)*output_layer[i].output_gradient(hidden_layer)[j]
                +alpha*output_layer[i].get_delta_w()[j]); //multiply output-gradients with learning rate and add learning rate*delta-weight

                output_layer[i].set_delta_w(j,(1-alpha)*output_layer[i].output_gradient(hidden_layer)[j]
                +alpha*output_layer[i].get_delta_w()[j]); //update the delta-weights with previous formula

            }
        }

        for(int i=0; i<hidden_layer.length; i++){ //continue with hidden-layer

            for(int j=0; j<input_layer.length; j++){ //number of weights for input-neurons = #neurons of hidden.layer

                input_layer[j].set_weight(i,(1-alpha)*hidden_layer[i].hidden_gradient(input_layer,output_layer)[j]
                +alpha*hidden_layer[i].get_delta_w()[j]); //multiply hidden-gradients with learning rate and add learning rate*delta-weight
                hidden_layer[i].set_delta_w(j,(1-alpha)*hidden_layer[i].hidden_gradient(input_layer,output_layer)[j]
                +alpha*hidden_layer[i].get_delta_w()[j]); //update the delta-weights with previous formula

            }
        }

    }

}

