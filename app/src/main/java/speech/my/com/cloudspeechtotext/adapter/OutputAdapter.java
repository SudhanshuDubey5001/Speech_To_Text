package speech.my.com.cloudspeechtotext.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.ref.PhantomReference;
import java.util.ArrayList;

import speech.my.com.cloudspeechtotext.R;
import speech.my.com.cloudspeechtotext.activity.FacebookActivity;
import speech.my.com.cloudspeechtotext.activity.OutputActivity;
import speech.my.com.cloudspeechtotext.other.Output;

public class OutputAdapter extends RecyclerView.Adapter<OutputAdapter.OutputViewHolder> {

    public static final String OUTPUT_OBJECT="object";

    private ArrayList<Output> outputs;
    private OutputActivity activity;

    public void outputElements(ArrayList<Output> o, OutputActivity activity) {
        this.outputs = o;
        this.activity=activity;
    }

    @Override
    public OutputViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.output_recyclerview, parent, false);
        return new OutputViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(OutputViewHolder holder, int position) {
        holder.outputNumber.setText("Output " + outputs.get(position).output_number);
        holder.outputEmail.setText("Email: " + outputs.get(position).output_email);
        holder.outputPass.setText("Password: " + outputs.get(position).output_pass);
        holder.pos = position;
    }

    @Override
    public int getItemCount() {
        return outputs.size();
    }

    //Viewholder inner class--------------------------------------->
    class OutputViewHolder extends RecyclerView.ViewHolder {

        public TextView outputNumber;
        public TextView outputEmail;
        public TextView outputPass;
        public int pos;

        OutputViewHolder(View itemView) {
            super(itemView);
            outputNumber = itemView.findViewById(R.id.outputNumber);
            outputEmail = itemView.findViewById(R.id.outputEmail);
            outputPass = itemView.findViewById(R.id.outputPass);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent in =new Intent(activity, FacebookActivity.class);
                    in.putExtra(OUTPUT_OBJECT,outputs.get(pos));
                    activity.startActivity(in);
                    activity.finish();
                }
            });
        }
    }
}