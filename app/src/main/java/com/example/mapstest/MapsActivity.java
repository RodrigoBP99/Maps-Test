package com.example.mapstest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;


public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private int PERMISSION_CODE = 123;
    private static final float ZOOM_CAMERA = 15f;
    private int i = 1;
    private double longitude;
    private double latitude;
    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if (possuiPermissao()) {
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMyLocationEnabled(true);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.setOnPoiClickListener(new GoogleMap.OnPoiClickListener() {
            @Override
            public void onPoiClick(PointOfInterest pointOfInterest) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                builder.setTitle(pointOfInterest.name).setMessage("Este ponto está em:\n" + "Latitude: " + pointOfInterest.latLng.latitude
                        + "\nLongitude: " + pointOfInterest.latLng.longitude).setPositiveButton("Ok", null).create().show();
            }
        });
    }

    private boolean possuiPermissao() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            String[] permissoes = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            ActivityCompat.requestPermissions(MapsActivity.this, permissoes, PERMISSION_CODE);
            return false;
        }
        return true;
    }

    private void pegarMinhaPosicao() {
        mMap.clear();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (location != null) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();

            moverMapa();
        }
    }

    private void moverMapa() {
        LatLng latLng = new LatLng(latitude, longitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_CAMERA));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        pegarMinhaPosicao();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onMapLongClick(final LatLng latLng) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setTitle("Novo Marker").setMessage("Deseja criar um novo ponto no mapa?").setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mMap.addMarker(new MarkerOptions().position(latLng).title("Marker " + i));
                i ++;
            }
        }).setNegativeButton("Não", null).show();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setTitle(marker.getTitle()).setMessage("Este ponto está em:\n" + "Latitude: " + marker.getPosition().latitude
                + "\nLongitude: " + marker.getPosition().longitude).setPositiveButton("Ok", null).create().show();
        return false;
    }

    @Override
    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }
}
