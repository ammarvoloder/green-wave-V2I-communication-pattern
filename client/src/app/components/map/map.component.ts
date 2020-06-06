import { Component, OnInit, ViewChild, NgZone, ElementRef, AfterViewInit } from '@angular/core';
import { TrafficLight } from 'src/app/models/traffic-light';
import { RestService } from 'src/app/services/rest-service';
import * as Stomp from 'stompjs';
import * as SockJS from 'sockjs-client';
import { Movement } from 'src/app/models/movement';
import { Vehicle } from 'src/app/models/vehicle';
import { element } from 'protractor';

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

  trafficLights: TrafficLight[] = [];
  movements: Movement[] = [];
  vehicles: Vehicle[] = [];
  ws: any;
  redLight = 'assets/images/red.png';
  greenLight = 'assets/images/green.png';
  car = 'assets/images/car.png';
  infoContent: string;
  trafficLightMarkerMap: Map<Number, google.maps.Marker>;
  movementMarkerMap: Map<String, google.maps.Marker>;


  constructor(private restService: RestService) { 
    this.trafficLightMarkerMap = new Map();
    this.movementMarkerMap = new Map();
  }

  ngAfterViewInit(): void {
    this.mapInitializer();
    this.getAllTrafficLights();
    this.getAllVehicles();
    this.initSocketConnections();
  }

  mapInitializer(){
    this.map = new google.maps.Map(this.gmap.nativeElement, 
      this.mapOptions);
  }

  getAllTrafficLights() {
    this.restService.getAllTrafficLights().subscribe(response => {
      response.forEach(element => {
        var coordinates = new google.maps.LatLng(element.latitude, element.longitude);
        const marker = new google.maps.Marker;
        const trafficLight = this.createTrafficLight(element);
        marker.setPosition(coordinates);
        marker.setIcon(element.statusGreen ? this.greenLight : this.redLight);
        marker.setMap(this.map);
        this.addListenerToMarker(marker, trafficLight, null);
        this.trafficLights.push(trafficLight);
        this.trafficLightMarkerMap.set(trafficLight.id, marker);
      })
    });
  }

  getAllVehicles() {
    this.restService.getAllVehicles().subscribe(response => {
      response.forEach(element => {
        this.vehicles.push(new Vehicle(element.vin, element.model, element.producer));
      })
    })
  }

  createTrafficLight(element: any): TrafficLight {
    return new TrafficLight(element.id, element.longitude, element.latitude);
  }

  createMovement(element: any): Movement {
    return new Movement(element.vin, element.speed, element.longitude, element.latitude, element.crash, element.dateTime);
  }

  addListenerToMarker(marker: google.maps.Marker, trafficLight: TrafficLight, movement: Movement){
    if(trafficLight){
      var content = `<span style="white-space: pre;">${trafficLight.fullInfo()}</span>`;
    } else {
      var content = `<span style="white-space: pre;">${movement.fullInfo()}</span>`;
    }
    const infoWindow = new google.maps.InfoWindow;
    infoWindow.setContent(content);
    marker.addListener('click', () => {
      infoWindow.open(marker.getMap(), marker);
      setTimeout(function(){infoWindow.close();}, 5000);
    });
  }

  removeMovementeMarker(movement:Movement) {
    let marker = this.movementMarkerMap.get(movement.vin);
    if(marker){
      marker.setMap(null);
      this.movementMarkerMap.delete(movement.vin);
    }  
  }

  removeTrafficLightMarker(trafficLight:TrafficLight) {
    let marker = this.trafficLightMarkerMap.get(trafficLight.id);
    if(marker){
      marker.setMap(null);
      this.trafficLightMarkerMap.delete(trafficLight.id);
    }
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
          trafficLight.statusChange = tl['dateTime'];
          that.trafficLights[index] = trafficLight;
          var coordinates = new google.maps.LatLng(trafficLight.latitude, trafficLight.longitude);
          const marker = new google.maps.Marker;
          marker.setPosition(coordinates);
          marker.setIcon(trafficLight.statusGreen ? that.greenLight : that.redLight);
          marker.setMap(that.map);
          that.addListenerToMarker(marker, trafficLight, null);
          that.removeTrafficLightMarker(trafficLight);
          that.trafficLightMarkerMap.set(trafficLight.id, marker);

      });
      that.ws.subscribe("/movements", function(element) {
        let mvm = JSON.parse(element.body);
        let movement = that.createMovement(mvm);
        let vehicle = that.vehicles.find(v => v.vin === movement.vin);
        movement.vehicle = vehicle;
        var coordinates = new google.maps.LatLng(movement.latitude, movement.longitude);
        let marker = that.movementMarkerMap.get(movement.vin);
        if(marker){
          marker.setPosition(coordinates);
        }else{ 
          marker = new google.maps.Marker;
          marker.setPosition(coordinates);
          marker.setIcon(that.car);
          marker.setMap(that.map);
          that.addListenerToMarker(marker, null, movement);
          that.removeMovementeMarker(movement);
          that.movementMarkerMap.set(movement.vin, marker);
        }
      });
    })
  } 
}
