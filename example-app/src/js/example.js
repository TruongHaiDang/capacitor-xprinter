import { CapacitorXprinter } from 'capacitor-xprinter';

window.testEcho = () => {
    const inputValue = document.getElementById("echoInput").value;
    CapacitorXprinter.echo({ value: inputValue })
}
