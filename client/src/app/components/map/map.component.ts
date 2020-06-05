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
  movementMarkers: google.maps.Marker[] = [];
  trafficLightMarkers: google.maps.Marker[] = [];

  
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
        marker.setIcon(this.redLight);
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
      console.log(this.vehicles);
    })
  }

  createTrafficLight(element: any): TrafficLight {
    return new TrafficLight(element.id, element.longitude, element.latitude);
  }

  createMovement(element: any): Movement {
    return new Movement(element.vin, element.speed, element.longitude, element.latitude, element.crash);
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
      setTimeout(function(){infoWindow.close();}, 4000);
    });
  }

  // Removes the markers from the map, but keeps them in the array.
  clearMarkers() {
    console.log("sta posaljes")
    console.log(this.movementMarkers)
    for (var i = 0; i < this.movementMarkers.length; i++) {
      this.movementMarkers[i].setMap(null);
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
          let marker = that.trafficLightMarkerMap.get(trafficLight.id); 
          let options;
          marker.setIcon(trafficLight.statusGreen ? that.greenLight : that.redLight);
          that.addListenerToMarker(marker, trafficLight, null);
      });
      that.ws.subscribe("/movements", function(element) {
        let mvm = JSON.parse(element.body);
        let movement = that.createMovement(mvm);
        let vehicle = that.vehicles.find(v => v.vin === movement.vin);
        console.log("printing vehicle:" + movement.latitude);
        movement.vehicle = vehicle;
        var coordinates = new google.maps.LatLng(movement.latitude, movement.longitude);
        const marker = new google.maps.Marker;
        marker.setPosition(coordinates);
        marker.setIcon(that.car);
        marker.setMap(that.map);
        that.clearMarkers();
        that.movementMarkers.push(marker);
        that.addListenerToMarker(marker, null, movement);
        that.movementMarkerMap.set(movement.vin, marker);
      });
    })
  } 
}
