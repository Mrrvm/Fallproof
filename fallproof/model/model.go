package model

import (
	"gopkg.in/mgo.v2/bson"
)

type User struct {
	ID     bson.ObjectId `json:"id" query:"id,omitempty" bson:"_id,omitempty"`
	User   string        `json:"user,omitempty" query:"user,omitempty" bson:"user"`
	Device string        `json:"device,omitempty" query:"device,omitempty" bson:"device,omitempty"`
}

type Device struct {
	Device    string        `json:"device" bson:"device,omitempty"`
	Fell      bool          `json:"fell" bson:"fell"`
	Emergency bool          `json:"emergency" bson:"emergency"`
	Time      string        `json:"time" bson:"time"`
	AInfo     float64       `json:"ainfo" bson:"ainfo"`
	ID        bson.ObjectId `json:"id,omitempty" bson:"_id,omitempty"`
}
