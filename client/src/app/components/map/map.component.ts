import { Component, OnInit, ViewChild } from '@angular/core';
import { TrafficLight } from 'src/app/models/traffic-light';
import { RestService } from 'src/app/services/rest-service';
import { element } from 'protractor';
import { MapMarker, MapInfoWindow } from '@angular/google-maps';

@Component({
  selector: 'app-map',
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.css']
})
export class MapComponent implements OnInit {

  @ViewChild(MapInfoWindow, { static: false }) infoWindow: MapInfoWindow;
  center: google.maps.LatLng;
  coordinates = new google.maps.LatLng(48.16411, 16.34629);
  trafficLights: TrafficLight[];
  markers: google.maps.Marker[];
<<<<<<< HEAD
  trafficLightMap: Map<Number, google.maps.Marker>

  redLight = 'assets/images/red.png';
  options: any;
=======
  options: google.maps.MarkerOptions;

  


  redLight = 'assets/images/green.png';
>>>>>>> c72db718e8be5f4456b54ea8252e5b74a7015908
  infoContent = '';
  
  constructor(private restService: RestService) { 
    this.trafficLightMap = new Map()
  }

  ngOnInit(): void {
    this.trafficLights = []
    this.markers = []
    this.center = this.coordinates;
<<<<<<< HEAD
    //this.getAllTrafficLights();
    const marker = new google.maps.Marker;
    this.options = {
      icon: this.redLight,
    }
    let trafficLight = new TrafficLight(1, this.coordinates.lng(), this.coordinates.lat())
    this.trafficLights.push(trafficLight)
    marker.setPosition(this.coordinates);
    marker.setOptions(this.options);
    this.markers.push(marker);

    this.trafficLightMap.set(trafficLight.id, marker)
=======
    this.getAllTrafficLights();
    //this.initSocketConnections();
>>>>>>> c72db718e8be5f4456b54ea8252e5b74a7015908
  }

  openInfo(marker: MapMarker) {
    let id;
    for (let [key, value] of this.trafficLightMap.entries()) {
      if(value.getPosition() ==  marker.getPosition()){
        id = key
      }
    }
    let trafficLight = this.trafficLights.find(light => light.id === id)
    this.infoContent = trafficLight.fullInfo()
    console.log(this.infoContent)
    this.infoWindow.open(marker);
  }

  getAllTrafficLights(){
    this.restService.getAllTrafficLights().subscribe(response => {
      this.trafficLights = response;
      console.log(this.trafficLights);
      this.trafficLights.forEach(element => {
        var coordinates = new google.maps.LatLng(element.latitude, element.longitude);
        var marker = new google.maps.Marker;
        this.options = {
          icon: this.redLight, 
        }
        marker.setPosition(coordinates);
<<<<<<< HEAD
        marker.setOptions(options);
=======
>>>>>>> c72db718e8be5f4456b54ea8252e5b74a7015908
        this.markers.push(marker);
      })
    })
  }

}
