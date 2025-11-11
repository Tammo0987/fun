{ pkgs, ... }:

{
  languages = {
    scala.enable = true;
    java.gradle.enable = true;
  };

  packages = with pkgs; [
    texlive.combined.scheme-full
  ];
}
