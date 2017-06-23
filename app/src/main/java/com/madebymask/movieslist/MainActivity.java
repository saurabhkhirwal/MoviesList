package com.madebymask.movieslist;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.madebymask.movieslist.models.MovieModel;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView lv;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pd = new ProgressDialog(this);
        pd.setIndeterminate(true);
        pd.setCancelable(false);
        pd.setMessage("Loading...");

        lv = (ListView) findViewById(R.id.listMain);

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .defaultDisplayImageOptions(defaultOptions)
                .build();
        ImageLoader.getInstance().init(config);

        //new JSONTask().execute("https://jsonparsingdemo-cec5b.firebaseapp.com/jsonData/moviesDemoItem.txt");
        //new JSONTask().execute("https://jsonparsingdemo-cec5b.firebaseapp.com/jsonData/moviesDemoList.txt");
        //new JSONTask().execute("https://jsonparsingdemo-cec5b.firebaseapp.com/jsonData/moviesData.txt");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            new JSONTask().execute("https://jsonparsingdemo-cec5b.firebaseapp.com/jsonData/moviesData.txt");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class JSONTask extends AsyncTask<String, String, List<MovieModel>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd.show();
        }

        @Override
        protected List<MovieModel> doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {

                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                String finStr = buffer.toString();
                JSONObject parentObj = new JSONObject(finStr);
                JSONArray parentArr = parentObj.getJSONArray("movies");
                List<MovieModel> mMList = new ArrayList<>();
                Gson gson = new Gson();
                for (int i=0;i<parentArr.length();i++) {
                    JSONObject finalObj = parentArr.getJSONObject(i);
                    MovieModel movieModel = gson.fromJson(finalObj.toString(),MovieModel.class);

//                    MovieModel movieModel = new MovieModel();
//                    movieModel.setMovie(finalObj.getString("movie"));
//                    movieModel.setYear(finalObj.getInt("year"));
//                    movieModel.setRating((float) finalObj.getDouble("rating"));
//                    movieModel.setDirector(finalObj.getString("director"));
//                    movieModel.setDuration(finalObj.getString("duration"));
//                    movieModel.setTagline(finalObj.getString("tagline"));
//                    movieModel.setImage(finalObj.getString("image"));
//                    movieModel.setStory(finalObj.getString("story"));
//                    List<MovieModel.Cast> lst = new ArrayList<>();
//                    for (int j = 0; j< finalObj.getJSONArray("cast").length();j++) {
//                        MovieModel.Cast cast = new MovieModel.Cast();
//                        cast.setName(finalObj.getJSONArray("cast").getJSONObject(j).getString("name"));
//                        lst.add(cast);
//                    }
//                    movieModel.setCastList(lst);

                    mMList.add(movieModel);
                }
                return mMList;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<MovieModel> result) {
            super.onPostExecute(result);
            pd.dismiss();
            MovieAdapter adapter = new MovieAdapter(getApplicationContext(),R.layout.row, result);
            lv.setAdapter(adapter);
            // TODO: need to set data to list
        }
    }

    public class MovieAdapter extends ArrayAdapter {

        private List<MovieModel> movieModelList;
        private int resource;
        private LayoutInflater inflater;

        public MovieAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<MovieModel> objects) {
            super(context, resource, objects);
            movieModelList = objects;
            this.resource = resource;
            inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            ViewHolder holder = null;

            if (convertView == null) {
                convertView = inflater.inflate(resource, null);
                holder = new ViewHolder();
                holder.ivIcon = (ImageView) convertView.findViewById(R.id.ivIcon);
                holder.tvMovie = (TextView) convertView.findViewById(R.id.tvMovie);
                holder.tvTagline = (TextView) convertView.findViewById(R.id.tvTagline);
                holder.tvYear = (TextView) convertView.findViewById(R.id.tvYear);
                holder.tvDuration = (TextView) convertView.findViewById(R.id.tvDuration);
                holder.tvDirector = (TextView) convertView.findViewById(R.id.tvDirector);
                holder.rbRating = (RatingBar) convertView.findViewById(R.id.rbRating);
                holder.tvCast = (TextView) convertView.findViewById(R.id.tvCast);
                holder.tvStory = (TextView) convertView.findViewById(R.id.tvStory);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final ProgressBar pbImage =  (ProgressBar) convertView.findViewById(R.id.pbImage);

            holder.tvMovie.setText(movieModelList.get(position).getMovie());
            holder.tvTagline.setText(movieModelList.get(position).getTagline());
            holder.tvYear.setText("Year: " + movieModelList.get(position).getYear());
            holder.tvDuration.setText("Duration: " + movieModelList.get(position).getDuration());
            holder.tvDirector.setText("Director: " + movieModelList.get(position).getDirector());
            holder.rbRating.setRating(movieModelList.get(position).getRating()/2);
            holder.tvStory.setText(movieModelList.get(position).getStory());
            StringBuffer bfr = new StringBuffer();
            for (MovieModel.Cast cast: movieModelList.get(position).getCastList()) {
                bfr.append(cast.getName() + ", ");
            }
            holder.tvCast.setText(bfr.toString());
            ImageLoader.getInstance().displayImage(movieModelList.get(position).getImage(), holder.ivIcon, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    pbImage.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    pbImage.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    pbImage.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                    pbImage.setVisibility(View.GONE);
                }
            });

            return convertView;
        }

        class ViewHolder {
            private ImageView ivIcon;
            private TextView tvMovie;
            private TextView tvTagline;
            private TextView tvYear;
            private TextView tvDuration;
            private TextView tvDirector;
            private RatingBar rbRating;
            private TextView tvCast;
            private TextView tvStory;
        }

    }
}
