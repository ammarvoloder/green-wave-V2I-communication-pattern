import { Component, OnInit, ViewChild, NgZone, ElementRef, AfterViewInit } from '@angular/core';
import { TrafficLight } from 'src/app/models/traffic-light';
import { RestService } from 'src/app/services/rest-service';
import * as Stomp from 'stompjs';
import * as SockJS from 'sockjs-client';

@Component({
  selector: 'app-map',
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.css']
})
export class MapComponent implements AfterViewInit {
  @ViewChild('mapContainer', {static: false}) gmap: ElementRef;
  
  map: google.maps.Map;
  coordinates = new google.maps.LatLng(48.16411, 16.34629);

  mapOptions: google.maps.MapOptions = {
    center: this.coordinates,
    zoom: 14.5
   };

  trafficLights: TrafficLight[];
  ws: any;
  redLight = 'assets/images/red.png';
  greenLight = 'assets/images/green.png';
  infoContent: string;
  trafficLightMarkerMap: Map<Number, google.maps.Marker>;

  
  constructor(private restService: RestService) { 
    this.trafficLightMarkerMap = new Map();
    this.trafficLights = [];
  }

  ngAfterViewInit(): void {
    this.mapInitializer();
    //this.getAllTrafficLights();
    //this.initSocketConnections();
  }

  mapInitializer(){
    this.map = new google.maps.Map(this.gmap.nativeElement, 
      this.mapOptions);
  }

  getAllTrafficLights(){
    this.restService.getAllTrafficLights().subscribe(response => {
      response.forEach(element => {
        var coordinates = new google.maps.LatLng(element.latitude, element.longitude);
        const marker = new google.maps.Marker;
        const trafficLight = this.createTrafficLight(element);
        marker.setPosition(coordinates);
        marker.setIcon(this.redLight);
        marker.setMap(this.map);
        this.addListenerToMarker(marker, trafficLight);
        this.trafficLights.push(trafficLight);
        this.trafficLightMarkerMap.set(trafficLight.id, marker);
      })
    });
  }

  createTrafficLight(element: any): TrafficLight {
    return new TrafficLight(element.id, element.longitude, element.latitude);
  }

  addListenerToMarker(marker: google.maps.Marker, trafficLight: TrafficLight){
    var content = `<span style="white-space: pre;">${trafficLight.fullInfo()}</span>`;
    const infoWindow = new google.maps.InfoWindow;
    infoWindow.setContent(content);
    marker.addListener('click', () => {
      infoWindow.open(marker.getMap(), marker);
      setTimeout(function(){infoWindow.close();}, 4000);
    });
  }

  initSocketConnections(){
    let ws = new SockJS("http://localhost:10113/ws");
    this.ws = Stomp.over(ws);
    let that = this;
    this.ws.connect({}, function(frame) {
      that.ws.subscribe("/trafficLights", function(element) {
          let tl = JSON.parse(element.body);
          let trafficLight = that.trafficLights.find(light => tl['trafficLightId'] === light.id);
          let index = that.trafficLights.indexOf(trafficLight);
          trafficLight.statusGreen = tl['green'];
          that.trafficLights[index] = trafficLight;
          let marker = that.trafficLightMarkerMap.get(trafficLight.id); 
          let options;
          if (trafficLight.statusGreen) {
            marker.setIcon(that.greenLight)
          } else {
            marker.setIcon(that.redLight);
          }
          that.addListenerToMarker(marker, trafficLight);
      });
    })
  } 
}
