import React from 'react';
import { connect } from 'react-redux';
import { Container, Card, CardHeader, CardBody, Button } from 'reactstrap';
import {verifyImage} from "../../services/auth.service";
import Webcam from 'react-webcam';
import sweetAlert from 'sweetalert';

class ValidatePhoto extends React.Component<any, any> {
  videoConstraints = {
    width: 360,
    height: 360,
    facingMode: 'user',
  };
  webcam: any;

  constructor(props:any) {
      super(props);
      this.webcam = React.createRef();
    this.handleClick = this.handleClick.bind(this);
  }

   convertBase64ToImage(img: string, sliceSize: number = 512) {
    const block = img.split(";");
    let contentType = block[0].split(":")[1];
    const b64Data = block[1].split(",")[1];
    contentType = contentType || '';

    const byteCharacters = atob(b64Data);
    let byteArrays = [];

    for (let offset = 0; offset < byteCharacters.length; offset += sliceSize) {
      const slice = byteCharacters.slice(offset, offset + sliceSize);

      let byteNumbers = new Array(slice.length);
      for (let i = 0; i < slice.length; i++) {
        byteNumbers[i] = slice.charCodeAt(i);
      }

      const byteArray = new Uint8Array(byteNumbers);

      byteArrays.push(byteArray);
    }

    const blob = new Blob(byteArrays, {type: contentType});
    return blob;
  }

  handleClick() {
    console.log(this.webcam);
    const sc = this.webcam.current.getScreenshot();
    verifyImage(this.props.user.user.id, this.convertBase64ToImage(sc)).then(res => {
      console.log(res);
      if(res.data) {
        sweetAlert({
          title: 'Accepted',
          text: 'User is valid',
          icon: 'success'
        });
      } else {
        sweetAlert({
          title: 'Unauthorized User.',
          icon: 'error'
        });
      }
    }).catch(err => {
      sweetAlert({
        title: 'Unauthorized User.',
        icon: 'error'
      });
    })
  }

  render() {
    return (
      <section className='user-profile'>
        <Container className='mt-4 login-container'>
          <Card className='bg-light medium-card text-center shadow border-0'>
            <CardHeader className='bg-primary text-light'>
              Validate Photo
            </CardHeader>
            <CardBody>
              <Webcam
                audio={false}
                screenshotFormat='image/jpeg'
                videoConstraints={this.videoConstraints}
                ref={this.webcam}
              ></Webcam>
              <Button color="primary" onClick={this.handleClick} className="mt-1">
                  Capture User
              </Button>
            </CardBody>
          </Card>
        </Container>
      </section>
    );
  }
}

const mapStateToProps = (state: any) => ({
  ...state,
  user: state.UserReducer,
});

export default connect(mapStateToProps)(ValidatePhoto);
