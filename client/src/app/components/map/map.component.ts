import { Component, OnInit, ViewChild, NgZone } from '@angular/core';
import { TrafficLight } from 'src/app/models/traffic-light';
import { RestService } from 'src/app/services/rest-service';
import { element } from 'protractor';
import { MapMarker, MapInfoWindow, GoogleMap } from '@angular/google-maps';
import * as Stomp from 'stompjs';
import * as SockJS from 'sockjs-client';

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
  //options: google.maps.MarkerOptions[];
  ws: any;
  redLight = 'assets/images/red.png';
  greenLight = 'assets/images/green.png';
  infoContent: string;
  trafficLightMap: Map<Number, google.maps.Marker>;

  
  constructor(private restService: RestService) { 
    this.trafficLightMap = new Map();
    this.trafficLights = [];
  }

  ngOnInit(): void {
    this.markers = []
    this.center = this.coordinates;
    this.getAllTrafficLights();
    this.initSocketConnections();
  }

  openInfo(marker: MapMarker) {
    let id;
    for (let [key, value] of this.trafficLightMap.entries()) {
      if(value.getPosition() ==  marker.getPosition()){
        id = key;
      }
    }
    let trafficLight = this.trafficLights.find(light => light.id === id);
    console.log(trafficLight);
    this.infoContent = trafficLight.fullInfo();
    this.infoWindow.open(marker);
  }

  getAllTrafficLights(){
    this.restService.getAllTrafficLights().subscribe(response => {
      response.forEach(element => {
        var coordinates = new google.maps.LatLng(element.latitude, element.longitude);
        const marker = new google.maps.Marker;
        marker.setPosition(coordinates);
        marker.setIcon(this.redLight);
        this.markers.push(marker);
        this.trafficLights.push(new TrafficLight(element.id, element.longitude, element.latitude));
        this.trafficLightMap.set(element.id, marker);
      })
    })
  }

  initSocketConnections(){
    let ws = new SockJS("http://localhost:10113/ws");
    this.ws = Stomp.over(ws);
    let that = this;
    this.ws.connect({}, function(frame) {
      that.ws.subscribe("/trafficLights", function(element) {
          console.log(element.body);
          let tl = JSON.parse(element.body);
          let trafficLight = that.trafficLights.find(light => tl['trafficLightId'] === light.id);
          let index = that.trafficLights.indexOf(trafficLight);
          trafficLight.statusGreen = tl['green'];
          that.trafficLights[index] = trafficLight;
          let marker = that.trafficLightMap.get(trafficLight.id); 
          let marker_index = that.markers.indexOf(marker);
          /* if (trafficLight.statusGreen) {
            that.options = {
              icon: that.greenLight
            }
          } else {
            that.options = {
              icon: that.redLight
            } 
          }*/
      });
    })
  }

}
